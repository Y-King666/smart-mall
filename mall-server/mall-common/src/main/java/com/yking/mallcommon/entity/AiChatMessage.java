package com.yking.mallcommon.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;


/**
 * AI 聊天消息实体类
 *
 * 对应数据库表：ai_chat_message
 * 存储用户与 AI 客服的对话记录
 *
 * 字段说明：
 * - id：主键，自增
 * - userId：发送消息的用户 ID（关联 sys_user.id）
 * - sessionId：会话 ID（UUID，同一会话的消息共享同一个 sessionId）
 * - role：消息角色（"user"=用户发送，"assistant"=AI 回复）
 * - content：消息内容
 * - createTime：消息发送时间
 *
 * 会话机制：
 * - 每个 sessionId 代表一次对话会话
 * - 用户可在同一会话中连续对话，AI 会参考历史消息上下文回复
 * - 前端展示会话列表时，按 sessionId 分组
 */
@TableName(value ="ai_chat_message")
@Data
public class AiChatMessage {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送消息的用户 ID */
    private Long userId;

    /** 会话 ID（UUID，同一会话的所有消息共享此 ID） */
    private String sessionId;

    /** 消息角色："user"=用户发送，"assistant"=AI 回复 */
    private String role;

    /** 消息内容（用户输入文本 或 AI 生成的回复） */
    private String content;

    /** 消息发送时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}