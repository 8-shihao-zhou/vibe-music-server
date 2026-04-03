package cn.edu.seig.vibemusic.agent.service;

import cn.edu.seig.vibemusic.agent.model.dto.AgentChatRequest;
import cn.edu.seig.vibemusic.agent.model.vo.AgentChatResponse;

/**
 * 智能体聊天服务接口
 *
 * 定义智能体对话的核心入口。
 */
public interface AgentChatService {

    /**
     * 根据用户输入生成智能体响应
     *
     * @param request 聊天请求
     * @return 聊天响应结果
     */
    AgentChatResponse chat(AgentChatRequest request);
}
