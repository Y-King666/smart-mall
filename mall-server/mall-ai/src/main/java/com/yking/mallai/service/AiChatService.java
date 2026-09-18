package com.yking.mallai.service;

import com.yking.mallai.dto.AiChatRequest;
import com.yking.mallai.dto.AiMessageVO;
import com.yking.mallai.dto.AiSessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 客服业务逻辑
 */
public interface AiChatService {

    /**
     * 发起一次流式对话
     *
     * 采用 SSE 逐字返回：模型每产出一段内容就推给前端，
     * 全部产出后再把完整的 AI 回复落库。
     *
     * @param userId  当前登录用户
     * @param request 会话标识与提问内容
     * @return SSE 连接对象
     */
    SseEmitter chat(Long userId, AiChatRequest request);

    /** 查询当前用户的会话列表，按最近对话时间倒序 */
    List<AiSessionVO> listSessions(Long userId);

    /**
     * 查询某个会话的历史消息
     *
     * 只能查自己的会话——按 userId 一起过滤，避免猜到 sessionId 就能读别人的对话
     */
    List<AiMessageVO> listMessages(Long userId, String sessionId);

    /**
     * 删除一个会话
     *
     * 同样是物理删除该会话下的全部消息；会话记录属于用户个人数据，
     * 用户要求删除时应当真正清掉，而不是留一条逻辑删除标记。
     *
     * @return 删除的消息条数，为 0 表示该会话不存在或不属于当前用户
     */
    int deleteSession(Long userId, String sessionId);

    /**
     * 清空当前用户的全部会话
     *
     * @return 删除的消息条数，为 0 表示本来就没有历史记录
     */
    int clearSessions(Long userId);
}
