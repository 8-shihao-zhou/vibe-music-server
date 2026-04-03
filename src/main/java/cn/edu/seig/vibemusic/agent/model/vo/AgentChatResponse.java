package cn.edu.seig.vibemusic.agent.model.vo;

import lombok.Data;

import java.util.List;


/**
 * Agent 聊天统一响应结构
 */
@Data
public class AgentChatResponse {

    /**
     * 返回给用户看的自然语言回复
     */
    private String reply;

    /**
     * 返回给前端执行的动作列表
     */
    private List<AgentActionVO> actions;

    /**
     * 工具层返回的结构化数据
     * 便于前端联调和后端排查
     */
    private AgentToolDataVO toolData;
}
