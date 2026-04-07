package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.enums.AgentActionType;
import cn.edu.seig.vibemusic.agent.model.vo.AgentActionVO;
import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内搜索工具。
 *
 * 说明：
 * 1. 歌曲搜索统一走全站综合搜索页 `/search`
 * 2. 歌手、歌单、社区搜索走各自页面，并带上 query 参数
 * 3. 这个工具只负责“搜索意图”，不负责播放歌曲
 */
@Component
@RequiredArgsConstructor
public class AgentSearchTool {

    private static final List<String> SONG_ALIASES = Arrays.asList("歌曲", "歌", "音乐", "单曲");
    private static final List<String> ARTIST_ALIASES = Arrays.asList("歌手", "艺人");
    private static final List<String> PLAYLIST_ALIASES = Arrays.asList("歌单", "收藏歌单", "歌单广场");
    private static final List<String> COMMUNITY_ALIASES = Arrays.asList("帖子", "社区", "动态", "讨论");

    private final AgentRuntimeContext agentRuntimeContext;

    /**
     * 让大模型解析站内搜索请求，并准备前端搜索动作。
     *
     * @param userMessage 用户原话
     * @return 便于模型理解的工具执行结果
     */
    @Tool("根据用户原话解析站内搜索意图。只适用于搜索、查找、搜一下、找一下等请求，不用于播放歌曲。歌曲搜索请准备全站综合搜索动作，其它类型准备对应页面搜索动作。")
    public String searchSite(String userMessage) {
        AgentToolDataVO data = resolveSearchData(userMessage);
        agentRuntimeContext.setToolData(data);

        if (!Boolean.TRUE.equals(data.getSuccess())) {
            return "未识别到明确的站内搜索意图";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("path", data.getPagePath());
        payload.put("pageName", data.getPageName());
        payload.put("searchType", data.getSearchType());
        payload.put("keyword", data.getSearchKeyword());

        agentRuntimeContext.addAction(new AgentActionVO(AgentActionType.SEARCH_SITE.getCode(), payload));

        return String.format(
                "已解析站内搜索：type=%s，keyword=%s，page=%s，已准备搜索动作",
                data.getSearchType(),
                data.getSearchKeyword(),
                data.getPageName()
        );
    }

    /**
     * 结构化搜索解析方法
     */
    public AgentToolDataVO resolveSearchData(String userMessage) {
        AgentToolDataVO data = new AgentToolDataVO();
        data.setSuccess(false);

        if (userMessage == null || userMessage.trim().isEmpty()) {
            data.setMessage("搜索内容为空");
            return data;
        }

        String normalized = normalizeMessage(userMessage);
        String searchType = detectSearchType(normalized);
        String keyword = extractKeyword(normalized, searchType);

        if (keyword == null || keyword.isBlank()) {
            data.setMessage("没有提取到明确的搜索关键词");
            return data;
        }

        data.setSuccess(true);
        data.setSearchType(searchType);
        data.setSearchKeyword(keyword);

        switch (searchType) {
            case "artist":
                data.setPagePath("/artist");
                data.setPageName("歌手页");
                break;
            case "playlist":
                data.setPagePath("/playlist");
                data.setPageName("歌单页");
                break;
            case "community":
                data.setPagePath("/community");
                data.setPageName("社区页");
                break;
            case "song":
            default:
                // 歌曲页本身没有搜索框，因此统一走全站综合搜索
                data.setPagePath("/search");
                data.setPageName("全站综合搜索");
                break;
        }

        data.setMessage("已解析站内搜索：" + keyword);
        return data;
    }

    /**
     * 判断搜索属于哪一类
     */
    private String detectSearchType(String message) {
        if (containsAny(message, ARTIST_ALIASES)) {
            return "artist";
        }
        if (containsAny(message, PLAYLIST_ALIASES)) {
            return "playlist";
        }
        if (containsAny(message, COMMUNITY_ALIASES)) {
            return "community";
        }
        return "song";
    }

    /**
     * 提取真正用于搜索的关键词
     */
    private String extractKeyword(String message, String searchType) {
        String keyword = message
                .replace("帮我", "")
                .replace("请帮我", "")
                .replace("我想", "")
                .replace("给我", "")
                .replace("搜索", "")
                .replace("搜一下", "")
                .replace("搜搜", "")
                .replace("查一下", "")
                .replace("查找", "")
                .replace("找一下", "")
                .replace("找找", "")
                .replace("站内", "")
                .replace("里面", "")
                .replace("相关", "")
                .replace("一个", "")
                .replace("看看", "")
                .replace("的", " ")
                .trim();

        if ("artist".equals(searchType)) {
            keyword = removeAliases(keyword, ARTIST_ALIASES);
        } else if ("playlist".equals(searchType)) {
            keyword = removeAliases(keyword, PLAYLIST_ALIASES);
        } else if ("community".equals(searchType)) {
            keyword = removeAliases(keyword, COMMUNITY_ALIASES);
        } else {
            keyword = removeAliases(keyword, SONG_ALIASES);
        }

        return keyword.replaceAll("\\s+", " ").trim();
    }

    private String removeAliases(String text, List<String> aliases) {
        String result = text;
        for (String alias : aliases) {
            result = result.replace(alias, "");
        }
        return result;
    }

    private boolean containsAny(String text, List<String> aliases) {
        for (String alias : aliases) {
            if (text.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeMessage(String userMessage) {
        return userMessage.trim().toLowerCase();
    }
}
