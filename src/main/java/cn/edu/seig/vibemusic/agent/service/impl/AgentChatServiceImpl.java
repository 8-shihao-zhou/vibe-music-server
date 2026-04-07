package cn.edu.seig.vibemusic.agent.service.impl;

import cn.edu.seig.vibemusic.agent.model.dto.AgentChatRequest;
import cn.edu.seig.vibemusic.agent.model.vo.AgentActionVO;
import cn.edu.seig.vibemusic.agent.model.vo.AgentChatResponse;
import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import cn.edu.seig.vibemusic.agent.service.AgentChatService;
import cn.edu.seig.vibemusic.agent.service.MusicAgentAssistant;
import cn.edu.seig.vibemusic.agent.tool.AgentRuntimeContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 聊天服务实现类。
 * 当前只负责：
 * 1. 将用户消息交给真正的 LangChain4j Agent
 * 2. 读取 Agent 在工具调用过程中写入的 actions 和 toolData
 * 3. 按前端现有协议返回 reply、actions、toolData
 */
@Service
@RequiredArgsConstructor
public class AgentChatServiceImpl implements AgentChatService {

    private final MusicAgentAssistant musicAgentAssistant;
    private final AgentRuntimeContext agentRuntimeContext;

    @Override
    public AgentChatResponse chat(AgentChatRequest request) {
        AgentChatResponse response = new AgentChatResponse();
        List<AgentActionVO> actions = new ArrayList<>();

        String message = request == null ? null : request.getMessage();
        if (message == null || message.trim().isEmpty()) {
            response.setReply("请输入你想让我帮你处理的内容。");
            response.setActions(actions);
            return response;
        }

        try {
            // 每次请求开始前初始化上下文，供工具记录结构化结果
            agentRuntimeContext.start(request.getUserId(), request.getSessionId());

            String reply = musicAgentAssistant.chat(message.trim());
            AgentRuntimeContext.State state = agentRuntimeContext.current();
            AgentToolDataVO toolData = state.getToolData();

            if (state.getActions() != null && !state.getActions().isEmpty()) {
                actions.addAll(state.getActions());
            }

            response.setReply(normalizeReply(reply, toolData, actions));
            response.setActions(actions);
            response.setToolData(toolData);
            return response;
        } catch (Exception e) {
            response.setReply("智能助手暂时不可用，请稍后再试。");
            response.setActions(actions);
            return response;
        } finally {
            agentRuntimeContext.clear();
        }
    }

    /**
     * 统一兜底回复，避免模型偶发返回空文本时影响前端展示。
     */
    private String normalizeReply(String reply, AgentToolDataVO toolData, List<AgentActionVO> actions) {
        if (reply != null && !reply.trim().isEmpty()) {
            return reply.trim();
        }

        if (toolData != null && Boolean.TRUE.equals(toolData.getSuccess())) {
            if (!actions.isEmpty() && toolData.getSongName() != null) {
                return "已为你找到相关歌曲，正在处理播放。";
            }
            if (!actions.isEmpty() && toolData.getPageName() != null && toolData.getSearchKeyword() == null) {
                return "已为你打开目标页面。";
            }
            if (!actions.isEmpty() && toolData.getSearchKeyword() != null) {
                return "已为你发起站内搜索。";
            }
            if (toolData.getUnreadCount() != null) {
                return "已查到你的未读通知数量。";
            }
            if (toolData.getAvailablePoints() != null || toolData.getTotalPoints() != null) {
                return "已查到你的积分信息。";
            }
        }

        if (toolData != null && toolData.getMessage() != null && !toolData.getMessage().isBlank()) {
            return toolData.getMessage();
        }

        return "好的，我已经收到你的请求。";
    }
}
