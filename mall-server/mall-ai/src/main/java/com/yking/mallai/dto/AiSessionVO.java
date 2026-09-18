package com.yking.mallai.dto;

import lombok.Data;

/**
 * 会话列表项
 */
@Data
public class AiSessionVO {

    /** 会话 ID */
    private String sessionId;

    /** 会话标题：取该会话中用户的第一句提问 */
    private String title;

    /** 最近一次对话时间，格式 yyyy-MM-dd HH:mm:ss */
    private String lastTime;

    /** 该会话的消息条数（含用户与 AI 双方） */
    private Long messageCount;
}
