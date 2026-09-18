package com.yking.mallai.dto;

import lombok.Data;

/**
 * 会话中的单条消息
 */
@Data
public class AiMessageVO {

    /** 角色：user=用户，assistant=AI */
    private String role;

    /** 消息内容 */
    private String content;

    /** 时间，格式 yyyy-MM-dd HH:mm:ss */
    private String createTime;
}
