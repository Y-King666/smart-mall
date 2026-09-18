package com.yking.mallai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * AI 客服对话请求
 */
@Data
public class AiChatRequest {

    /**
     * 会话 ID
     *
     * 由前端生成（新建会话时生成一个 UUID），服务端据此归档消息，
     * 同一会话的多轮对话共享同一个 ID
     */
    @NotBlank(message = "缺少会话标识")
    @Size(max = 64, message = "会话标识过长")
    private String sessionId;

    @NotBlank(message = "请输入要咨询的内容")
    @Size(max = 500, message = "单条提问不能超过 500 字")
    private String message;
}
