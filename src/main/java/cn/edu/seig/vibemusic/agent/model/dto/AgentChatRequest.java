package cn.edu.seig.vibemusic.agent.model.dto;

import lombok.Data;


/**
 * 智能体聊天请求 DTO
 *
 * 用于接收前端发送的聊天内容、会话标识以及当前用户 ID。
 */
@Data
public class AgentChatRequest {

    /**
     * 用户输入的聊天消息
     */
    private String message;

    /**
     * 会话 ID，用于前后端后续扩展多轮对话上下文
     */
    private String sessionId;

    /**
     * 当前发起请求的用户 ID
     */
    private Long userId;
}
