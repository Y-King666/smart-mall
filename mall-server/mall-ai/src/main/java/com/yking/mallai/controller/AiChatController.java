package com.yking.mallai.controller;

import com.yking.mallai.dto.AiChatRequest;
import com.yking.mallai.dto.AiMessageVO;
import com.yking.mallai.dto.AiSessionVO;
import com.yking.mallai.service.AiChatService;
import com.yking.mallcommon.Result;
import com.yking.mallcommon.exception.BusinessException;
import com.yking.mallcommon.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 用户端 AI 客服接口
 *
 * 需要登录：本模块的 /api/portal/ai/** 不在 SecurityConfig 的公开清单里，
 * 会走 anyRequest().authenticated()，因此会话是归属于具体用户的。
 */
@Slf4j
@RestController
@RequestMapping("/api/portal/ai")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 发起一次对话（SSE 流式返回）
     *
     * 返回的事件体是 JSON：{"type":"content","content":"..."} 逐段推送，
     * 结束时推 {"type":"done","sessionId":"..."}
     *
     * 注意这里不套 Result 统一响应体——SSE 是逐段流式输出，无法用一次性包装的结构承载。
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(Authentication authentication, @Valid @RequestBody AiChatRequest request) {
        return aiChatService.chat(AuthUtil.currentUserId(authentication), request);
    }

    /** 我的会话列表，按最近对话时间倒序 */
    @GetMapping("/sessions")
    public Result<List<AiSessionVO>> sessions(Authentication authentication) {
        return Result.success(aiChatService.listSessions(AuthUtil.currentUserId(authentication)));
    }

    /** 某个会话的历史消息 */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<AiMessageVO>> messages(Authentication authentication,
                                             @PathVariable String sessionId) {
        return Result.success(aiChatService.listMessages(AuthUtil.currentUserId(authentication), sessionId));
    }

    /**
     * 删除一个会话
     * 会话归属当前登录用户，删不到就说明它不存在或不属于你，统一按「会话不存在」提示
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Integer> deleteSession(Authentication authentication, @PathVariable String sessionId) {
        int removed = aiChatService.deleteSession(AuthUtil.currentUserId(authentication), sessionId);
        if (removed == 0) {
            throw new BusinessException("会话不存在");
        }
        return Result.success(removed);
    }

    /**
     * 清空当前用户的全部历史会话
     * 无需校验归属之外的条件，删 0 条也按成功返回（本来就没有历史不是错误）
     */
    @DeleteMapping("/sessions")
    public Result<Integer> clearSessions(Authentication authentication) {
        return Result.success(aiChatService.clearSessions(AuthUtil.currentUserId(authentication)));
    }
}
