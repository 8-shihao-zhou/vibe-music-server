package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Agent 站内搜索工具
 *
 * 作用：
 * 1. 识别用户是想搜歌曲、歌手、歌单还是社区帖子
 * 2. 提取真正的搜索关键词
 * 3. 返回前端跳转所需的页面路径和搜索参数
 */
@Component
public class AgentSearchTool {

    /**
     * 歌曲搜索相关别名
     */
    private static final List<String> SONG_ALIASES = Arrays.asList("歌曲", "歌", "音乐", "单曲");

    /**
     * 歌手搜索相关别名
     */
    private static final List<String> ARTIST_ALIASES = Arrays.asList("歌手", "歌手页", "艺人");

    /**
     * 歌单搜索相关别名
     */
    private static final List<String> PLAYLIST_ALIASES = Arrays.asList("歌单", "收藏歌单", "歌单广场");

    /**
     * 社区搜索相关别名
     */
    private static final List<String> COMMUNITY_ALIASES = Arrays.asList("帖子", "社区", "动态", "讨论");

    /**
     * 将自然语言搜索请求解析为结构化搜索结果
     *
     * @param userMessage 用户原始输入
     * @return 搜索类型、关键词以及目标页面等结构化数据
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
                data.setPagePath("/library");
                data.setPageName("音乐库");
                break;
        }

        data.setMessage("已解析站内搜索：" + keyword);
        return data;
    }

    /**
     * 检测当前搜索属于哪一种业务类型
     *
     * @param message 标准化后的消息
     * @return 搜索类型编码
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
     * 从用户输入中提取真正用于搜索的关键词
     *
     * @param message 标准化后的消息
     * @param searchType 当前识别出的搜索类型
     * @return 提取后的搜索关键词
     */
    private String extractKeyword(String message, String searchType) {
        String keyword = message;

        keyword = keyword
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
                .replace("一下", "")
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

    /**
     * 去除某一类搜索别名，尽量保留真正的搜索内容
     */
    private String removeAliases(String text, List<String> aliases) {
        String result = text;
        for (String alias : aliases) {
            result = result.replace(alias, "");
        }
        return result;
    }

    /**
     * 判断文本中是否包含任意一个别名
     */
    private boolean containsAny(String text, List<String> aliases) {
        for (String alias : aliases) {
            if (text.contains(alias)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 标准化用户输入，统一为小写并去掉首尾空白
     */
    private String normalizeMessage(String userMessage) {
        return userMessage.trim().toLowerCase();
    }
}
