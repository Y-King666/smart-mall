package com.yking.mallai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yking.mallai.dto.AiChatRequest;
import com.yking.mallai.dto.AiMessageVO;
import com.yking.mallai.dto.AiSessionVO;
import com.yking.mallai.function.CartFunction;
import com.yking.mallai.function.OrderFunction;
import com.yking.mallai.function.ProductFunction;
import com.yking.mallai.service.AiChatService;
import com.yking.mallcommon.entity.AiChatMessage;
import com.yking.mallcommon.mapper.AiChatMessageMapper;
import com.yking.mallcommon.mapper.OmsCartItemMapper;
import com.yking.mallcommon.mapper.OmsOrderItemMapper;
import com.yking.mallcommon.mapper.OmsOrderMapper;
import com.yking.mallcommon.mapper.PmsProductMapper;
import com.yking.mallcommon.service.OrderCancelService;
import com.yking.mallcommon.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    /** 消息角色：用户 */
    private static final String ROLE_USER = "user";

    /** 消息角色：AI */
    private static final String ROLE_ASSISTANT = "assistant";

    /**
     * 每次请求最多带多少条历史消息给模型
     *
     * 上下文越长越贵，且超出模型窗口会被截断，这里限制在最近若干条
     */
    private static final int HISTORY_LIMIT = 20;

    /** SSE 连接超时时间（毫秒） */
    private static final long SSE_TIMEOUT = 60_000L;

    /** 会话标题的最大展示长度，超出截断 */
    private static final int TITLE_MAX_LENGTH = 18;

    /** 系统提示词：把客服角色、经营品类与可用工具讲清楚，并明确禁止编造数据 */
    private static final String SYSTEM_PROMPT = """
            你是「严选商城」的智能客服顾问。
            商城的商品均为虚拟商品，包括在线课程、软件授权、电子书与会员服务，下单后无需物流配送。
            你的职责：介绍商品、解答选购疑问、说明下单支付与售后流程。
            商城内置了四个查询工具和一个操作工具：
            1. getProductInfo（按名称关键字模糊查询商品，返回商品名称、价格、库存与描述）
            2. getHotProducts（按销量从高到低返回热销商品，用于推荐好物）
            3. getMyOrders（查询用户自己的历史订单，返回订单号、下单时间、金额、支付状态与商品明细）
            4. getMyCart（查询用户自己的购物车，返回商品、数量、选中状态与已选中商品的总金额）
            5. cancelMyOrder（按订单号取消用户自己的一笔订单，会真的改动数据）
            凡涉及商品名称、价格、库存，或用户自己的订单与购物车，必须先调用工具核实，严禁编造数据。
            商品查询按名称关键字模糊匹配，关键字取短一些（例如查会员类商品用「会员」而不是「会员服务」）；
            第一次没查到就换一个更短的关键字再查一次，确实是商城没有的商品才告知用户查不到。
            订单相关的规则：用户可在「我的订单」页查看、支付或取消订单；待支付与已支付的订单都可以取消，
            取消后对应商品的库存在商城内恢复；虚拟商品无需物流配送，支付成功即完成购买。
            购物车相关的规则：用户可在「购物车」页勾选商品后提交订单，结算只计算已勾选的商品。
            代替用户取消订单的规则（涉及改动数据，务必遵守）：
            用户说要取消订单但没说订单号时，先问清是哪一笔，不要猜、更不要编造订单号；
            可以调用 getMyOrders 把最近的订单列给他参考，请他告知要取消哪一单。
            只有用户明确要求取消、并且已经给出订单号时，才调用 cancelMyOrder。
            取消成功后只告知「订单已取消」，不必说明库存或销量之类的内部变化，最后问一句还有什么可以帮忙；
            失败时把失败原因转述给用户，不要自行编造结果。
            用中文回答，语气专业、简洁、友善；查不到或不确定的内容如实说明。
            回答用短句或分行短列表，多条订单也逐条分行说明，不要使用 Markdown 表格。
            只说明商城实际提供的功能，不要承诺未提供的服务（发票、物流、补发虚拟码等）。
            """;

    /** 模型不可用时的兜底回复（同样落库，保证会话记录与用户所见一致） */
    private static final String FALLBACK_REPLY = "抱歉，AI 服务暂时不可用，请稍后再试。";

    private final ChatModel chatModel;
    private final ProductFunction productFunction;
    private final AiChatMessageMapper aiChatMessageMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsCartItemMapper omsCartItemMapper;
    private final PmsProductMapper pmsProductMapper;
    private final OrderCancelService orderCancelService;
    private final QuestionAnswerAdvisor questionAnswerAdvisor;

    public AiChatServiceImpl(ChatModel chatModel,
                             ProductFunction productFunction,
                             AiChatMessageMapper aiChatMessageMapper,
                             OmsOrderMapper omsOrderMapper,
                             OmsOrderItemMapper omsOrderItemMapper,
                             OmsCartItemMapper omsCartItemMapper,
                             PmsProductMapper pmsProductMapper,
                             OrderCancelService orderCancelService,
                             QuestionAnswerAdvisor questionAnswerAdvisor) {
        this.chatModel = chatModel;
        this.productFunction = productFunction;
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.omsOrderMapper = omsOrderMapper;
        this.omsOrderItemMapper = omsOrderItemMapper;
        this.omsCartItemMapper = omsCartItemMapper;
        this.pmsProductMapper = pmsProductMapper;
        this.orderCancelService = orderCancelService;
        this.questionAnswerAdvisor = questionAnswerAdvisor;
    }

    @Override
    public SseEmitter chat(Long userId, AiChatRequest request) {
        String sessionId = request.getSessionId();

        // 1. 先把用户这条提问落库
        saveMessage(userId, sessionId, ROLE_USER, request.getMessage());

        // 2. 取该会话最近若干条消息作为上下文（含刚写入的这条）
        List<Message> history = loadHistory(userId, sessionId);

        // 3. 组装带系统提示词、查询工具与知识库检索的对话客户端
        //    订单与购物车工具按用户新建，把 userId 绑死在这个实例上，
        //    模型只能查到当前用户自己的数据，也不会把别人的订单算进来
        //    知识库 advisor 会让每次提问先检索一次 PDF，把相关片段拼进 prompt
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(productFunction,
                        new OrderFunction(userId, omsOrderMapper, omsOrderItemMapper, orderCancelService),
                        new CartFunction(userId, omsCartItemMapper, pmsProductMapper))
                .defaultAdvisors(questionAnswerAdvisor)
                .build();

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        StringBuilder reply = new StringBuilder();

        // 兜底放在最前面：模型异常时把错误转成一段正常内容，
        // 这样它既能推给前端，也会被累加进 reply 一并落库
        Flux<String> stream = chatClient.prompt()
                .messages(history)
                .stream()
                .content()
                .onErrorResume(error -> {
                    log.error("AI 对话失败，sessionId={}", sessionId, error);
                    return Flux.just(FALLBACK_REPLY);
                });

        stream.doOnNext(chunk -> {
                    reply.append(chunk);
                    sendEvent(emitter, Map.of("type", "content", "content", chunk));
                })
                .doOnComplete(() -> {
                    saveMessage(userId, sessionId, ROLE_ASSISTANT, reply.toString());
                    sendEvent(emitter, Map.of("type", "done", "sessionId", sessionId));
                    emitter.complete();
                })
                .subscribe();

        return emitter;
    }

    @Override
    public List<AiSessionVO> listSessions(Long userId) {
        // 按会话聚合出最近对话时间与消息条数
        QueryWrapper<AiChatMessage> wrapper = new QueryWrapper<>();
        wrapper.select("session_id", "MAX(create_time) AS last_time", "COUNT(*) AS message_count")
                .eq("user_id", userId)
                .groupBy("session_id")
                .orderByDesc("last_time");
        List<Map<String, Object>> rows = aiChatMessageMapper.selectMaps(wrapper);
        if (rows.isEmpty()) {
            return List.of();
        }

        // 一次取出该用户所有提问，用于给每个会话取标题（会话里的第一句提问）
        List<AiChatMessage> userMessages = aiChatMessageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getUserId, userId)
                        .eq(AiChatMessage::getRole, ROLE_USER)
                        .orderByAsc(AiChatMessage::getId));
        Map<String, String> titleBySession = new HashMap<>();
        for (AiChatMessage message : userMessages) {
            titleBySession.putIfAbsent(message.getSessionId(), message.getContent());
        }

        List<AiSessionVO> sessions = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            String sessionId = String.valueOf(row.get("session_id"));
            AiSessionVO session = new AiSessionVO();
            session.setSessionId(sessionId);
            session.setTitle(abbreviate(titleBySession.getOrDefault(sessionId, "新会话")));
            session.setLastTime(formatTimeValue(row.get("last_time")));
            session.setMessageCount(((Number) row.get("message_count")).longValue());
            sessions.add(session);
        }
        return sessions;
    }

    @Override
    public List<AiMessageVO> listMessages(Long userId, String sessionId) {
        List<AiChatMessage> messages = aiChatMessageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getUserId, userId)
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByAsc(AiChatMessage::getId));

        List<AiMessageVO> result = new ArrayList<>(messages.size());
        for (AiChatMessage message : messages) {
            AiMessageVO vo = new AiMessageVO();
            vo.setRole(message.getRole());
            vo.setContent(message.getContent());
            vo.setCreateTime(DateTimeUtil.format(message.getCreateTime()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public int deleteSession(Long userId, String sessionId) {
        // 按 userId 一起过滤，避免猜到 sessionId 就能删掉别人的会话
        return aiChatMessageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getUserId, userId)
                .eq(AiChatMessage::getSessionId, sessionId));
    }

    @Override
    public int clearSessions(Long userId) {
        // 只删当前用户自己的记录
        return aiChatMessageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getUserId, userId));
    }

    /** 写入一条消息，createTime 由 MyBatis-Plus 的自动填充处理器写入 */
    private void saveMessage(Long userId, String sessionId, String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setUserId(userId);
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        aiChatMessageMapper.insert(message);
    }

    /** 取会话最近若干条消息，按时间正序返回给模型 */
    private List<Message> loadHistory(Long userId, String sessionId) {
        List<AiChatMessage> recent = aiChatMessageMapper.selectList(
                new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getUserId, userId)
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByDesc(AiChatMessage::getId)
                        // 常量拼接，无外部输入
                        .last("limit " + HISTORY_LIMIT));
        Collections.reverse(recent);
        return recent.stream()
                .map(message -> ROLE_USER.equals(message.getRole())
                        ? (Message) new UserMessage(message.getContent())
                        : (Message) new AssistantMessage(message.getContent()))
                .toList();
    }

    /** 推送一个 SSE 事件；前端可能已断开，发送失败不影响后续流程 */
    private void sendEvent(SseEmitter emitter, Map<String, Object> payload) {
        try {
            emitter.send(SseEmitter.event().data(payload, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.debug("SSE 推送失败（前端可能已断开）：{}", e.getMessage());
        }
    }

    /** 会话标题过长时截断 */
    private String abbreviate(String text) {
        String plain = text.replaceAll("\\s+", " ").trim();
        return plain.length() <= TITLE_MAX_LENGTH ? plain : plain.substring(0, TITLE_MAX_LENGTH) + "…";
    }

    /**
     * 把聚合查询取回的时间值格式化为字符串
     *
     * selectMaps 取回的 DATETIME 具体类型随驱动版本而变（LocalDateTime / Timestamp / Date），
     * 这里逐个兼容，避免不同环境表现不一致
     */
    private String formatTimeValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime dateTime) {
            return DateTimeUtil.format(dateTime);
        }
        if (value instanceof Timestamp timestamp) {
            return DateTimeUtil.format(timestamp.toLocalDateTime());
        }
        if (value instanceof Date date) {
            return DateTimeUtil.format(date);
        }
        return String.valueOf(value);
    }
}
