package cn.edu.seig.vibemusic.agent.service.impl;

import cn.edu.seig.vibemusic.agent.enums.AgentActionType;
import cn.edu.seig.vibemusic.agent.model.dto.AgentChatRequest;
import cn.edu.seig.vibemusic.agent.model.vo.AgentActionVO;
import cn.edu.seig.vibemusic.agent.model.vo.AgentChatResponse;
import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import cn.edu.seig.vibemusic.agent.service.AgentChatService;
import cn.edu.seig.vibemusic.agent.service.MusicAgentAssistant;
import cn.edu.seig.vibemusic.agent.tool.AgentMusicTool;
import cn.edu.seig.vibemusic.agent.tool.AgentNavigationTool;
import cn.edu.seig.vibemusic.agent.tool.AgentUserTool;
import cn.edu.seig.vibemusic.agent.tool.AgentSearchTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 聊天服务实现类
 *
 * 最终职责：
 * 1. 判断用户意图
 * 2. 调用真实工具查询数据
 * 3. 组装前端动作协议
 * 4. 调用 LangChain4j 生成自然语言回复
 */
@Service
@RequiredArgsConstructor
public class AgentChatServiceImpl implements AgentChatService {

    private final MusicAgentAssistant musicAgentAssistant;
    private final AgentMusicTool agentMusicTool;
    private final AgentNavigationTool agentNavigationTool;
    private final AgentUserTool agentUserTool;
    private final AgentSearchTool agentSearchTool;


    @Override
    public AgentChatResponse chat(AgentChatRequest request) {
        String message = request.getMessage();
        Long userId = request.getUserId();

        AgentChatResponse response = new AgentChatResponse();
        List<AgentActionVO> actions = new ArrayList<>();

        // 防御性处理，避免空消息导致异常
        if (message == null || message.trim().isEmpty()) {
            response.setReply("请输入你想让我帮你做的事情。");
            response.setActions(actions);
            return response;
        }

        String trimmedMessage = message.trim();

        // 1. 播放歌曲意图
        if (isPlayIntent(trimmedMessage)) {
            AgentToolDataVO toolData = agentMusicTool.searchSongData(trimmedMessage);
            response.setToolData(toolData);

            if (Boolean.TRUE.equals(toolData.getSuccess())) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("songId", toolData.getSongId());
                payload.put("songName", toolData.getSongName());
                payload.put("artistName", toolData.getArtistName());
                payload.put("coverUrl", toolData.getCoverUrl());
                payload.put("audioUrl", toolData.getAudioUrl());

                actions.add(new AgentActionVO(AgentActionType.PLAY_SONG.getCode(), payload));

                String prompt = String.format(
                        "用户说：%s。系统已经找到歌曲《%s》-%s，即将开始播放。请用一句简短中文告诉用户。",
                        trimmedMessage,
                        toolData.getSongName(),
                        toolData.getArtistName()
                );
                response.setReply(musicAgentAssistant.chat(prompt));
            } else {
                response.setReply("暂时没有找到匹配的歌曲，你可以换个关键词再试试。");
            }

            response.setActions(actions);
            return response;
        }

        // 2. 站内搜索意图
        if (isSearchIntent(trimmedMessage)) {
            AgentToolDataVO toolData = agentSearchTool.resolveSearchData(trimmedMessage);
            response.setToolData(toolData);

            if (Boolean.TRUE.equals(toolData.getSuccess())) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("path", toolData.getPagePath());
                payload.put("pageName", toolData.getPageName());
                payload.put("searchType", toolData.getSearchType());
                payload.put("keyword", toolData.getSearchKeyword());

                actions.add(new AgentActionVO(AgentActionType.SEARCH_SITE.getCode(), payload));

                String prompt = String.format(
                        "用户说：%s。系统已识别为站内搜索，搜索类型是%s，关键词是%s，将跳转到%s。请用一句简短中文回复用户。",
                        trimmedMessage,
                        toolData.getSearchType(),
                        toolData.getSearchKeyword(),
                        toolData.getPageName()
                );
                response.setReply(musicAgentAssistant.chat(prompt));
            } else {
                response.setReply("我还没识别出你想搜索什么内容，你可以直接说例如“搜索周杰伦的歌曲”或“搜索治愈系歌单”。");
            }

            response.setActions(actions);
            return response;
        }

        // 3. 页面跳转意图
        if (isNavigateIntent(trimmedMessage)) {
            AgentToolDataVO toolData = agentNavigationTool.resolvePageData(trimmedMessage);
            response.setToolData(toolData);

            if (Boolean.TRUE.equals(toolData.getSuccess())) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("path", toolData.getPagePath());
                payload.put("pageName", toolData.getPageName());

                actions.add(new AgentActionVO(AgentActionType.NAVIGATE_TO.getCode(), payload));

                String prompt = String.format(
                        "用户说：%s。系统已经识别到目标页面是%s，路径是%s。请用一句简短中文告诉用户已经准备跳转。",
                        trimmedMessage,
                        toolData.getPageName(),
                        toolData.getPagePath()
                );
                response.setReply(musicAgentAssistant.chat(prompt));
            } else {
                response.setReply("我还没识别出你想打开哪个页面，你可以说得更具体一点。");
            }

            response.setActions(actions);
            return response;
        }

        // 4. 未读通知查询
        if (isUnreadNotificationIntent(trimmedMessage)) {
            AgentToolDataVO toolData = agentUserTool.getUnreadNotificationCountData(userId);
            response.setToolData(toolData);

            if (Boolean.TRUE.equals(toolData.getSuccess())) {
                String prompt = String.format(
                        "用户问：%s。系统查到未读通知数量为 %d。请用一句简短中文回答用户。",
                        trimmedMessage,
                        toolData.getUnreadCount()
                );
                response.setReply(musicAgentAssistant.chat(prompt));
            } else {
                response.setReply("暂时没能查到你的未读通知数量。");
            }

            response.setActions(actions);
            return response;
        }

        // 5. 积分查询
        if (isPointsIntent(trimmedMessage)) {
            AgentToolDataVO toolData = agentUserTool.getUserPointsData(userId);
            response.setToolData(toolData);

            if (Boolean.TRUE.equals(toolData.getSuccess())) {
                String prompt = String.format(
                        "用户问：%s。系统查到可用积分为 %d，总积分为 %d。请用一句简短中文回答用户。",
                        trimmedMessage,
                        toolData.getAvailablePoints(),
                        toolData.getTotalPoints()
                );
                response.setReply(musicAgentAssistant.chat(prompt));
            } else {
                response.setReply("暂时没能查到你的积分信息。");
            }

            response.setActions(actions);
            return response;
        }

        // 6. 普通问答，直接交给模型回复
        response.setReply(musicAgentAssistant.chat(trimmedMessage));
        response.setActions(actions);
        return response;
    }

    /**
     * 判断是否是播放歌曲意图
     */
    private boolean isPlayIntent(String message) {
        return message.contains("播放")
                || message.contains("来一首")
                || message.contains("来首")
                || message.contains("听")
                || message.contains("放一首")
                || message.contains("给我放");
    }

    /**
     * 判断是否是页面跳转意图
     */
    private boolean isNavigateIntent(String message) {
        return message.contains("打开")
                || message.contains("进入")
                || message.contains("跳转")
                || message.contains("去")
                || message.contains("前往");
    }

    /**
     * 判断是否是未读通知相关意图
     */
    private boolean isUnreadNotificationIntent(String message) {
        return message.contains("未读通知")
                || message.contains("多少通知")
                || message.contains("多少条通知")
                || message.contains("通知数量");
    }

    /**
     * 判断是否是积分查询意图
     */
    private boolean isPointsIntent(String message) {
        return message.contains("积分")
                || message.contains("可用积分")
                || message.contains("总积分");
    }

    /**
     * 判断是否是站内搜索意图
     */
    private boolean isSearchIntent(String message) {
        return message.contains("搜索")
                || message.contains("搜一下")
                || message.contains("搜搜")
                || message.contains("查一下")
                || message.contains("查找")
                || message.contains("找一下")
                || message.contains("找找");
    }

}
