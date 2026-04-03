package cn.edu.seig.vibemusic.agent.tool;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Agent 页面导航注册表
 *
 * 作用：
 * 1. 统一维护所有可跳转页面
 * 2. 为每个页面配置多个中文别名，方便自然语言匹配
 * 3. 后续新增页面时，只需要在这里补一条配置即可
 */
public final class AgentPageRegistry {

    private AgentPageRegistry() {
    }

    /**
     * 当前前端所有静态页面路由映射
     */
    public static final List<PageMapping> PAGE_MAPPINGS = Collections.unmodifiableList(Arrays.asList(
            new PageMapping("首页", "/", Arrays.asList(
                    "首页", "主页", "主页面", "推荐页", "发现页"
            )),
            new PageMapping("音乐库", "/library", Arrays.asList(
                    "音乐库", "曲库", "歌曲库", "音乐列表"
            )),
            new PageMapping("歌手页", "/artist", Arrays.asList(
                    "歌手页", "歌手", "歌手列表"
            )),
            new PageMapping("歌单页", "/playlist", Arrays.asList(
                    "歌单页", "歌单", "歌单广场", "歌单列表"
            )),
            new PageMapping("我喜欢", "/like", Arrays.asList(
                    "我喜欢", "喜欢的音乐", "收藏歌曲", "喜欢页面"
            )),
            new PageMapping("个人中心", "/user", Arrays.asList(
                    "个人中心", "我的主页", "我的资料", "我的页面", "个人页"
            )),
            new PageMapping("收藏歌单", "/favorite-playlists", Arrays.asList(
                    "收藏歌单", "我收藏的歌单", "歌单收藏"
            )),
            new PageMapping("AI创作", "/ai", Arrays.asList(
                    "ai", "AI", "ai创作", "AI创作", "AI功能", "AI页面"
            )),
            new PageMapping("社区", "/community", Arrays.asList(
                    "社区", "音乐社区", "动态广场", "社区首页"
            )),
            new PageMapping("发布帖子", "/community/create", Arrays.asList(
                    "发布帖子", "发帖", "写帖子", "发布动态"
            )),
            new PageMapping("我的草稿", "/community/drafts", Arrays.asList(
                    "草稿箱", "我的草稿", "帖子草稿"
            )),
            new PageMapping("我的收藏", "/community/favorite", Arrays.asList(
                    "社区收藏", "帖子收藏", "我的收藏", "收藏帖子"
            )),
            new PageMapping("标签广场", "/community/tags", Arrays.asList(
                    "标签广场", "社区标签", "标签页"
            )),
            new PageMapping("通知中心", "/notification", Arrays.asList(
                    "通知中心", "通知", "消息通知", "我的通知"
            )),
            new PageMapping("积分中心", "/points", Arrays.asList(
                    "积分中心", "积分", "我的积分"
            )),
            new PageMapping("积分商城", "/mall", Arrays.asList(
                    "积分商城", "商城", "兑换商城"
            )),
            new PageMapping("每日推荐", "/daily", Arrays.asList(
                    "每日推荐", "推荐歌曲", "今日推荐"
            )),
            new PageMapping("曲风分类", "/genre", Arrays.asList(
                    "曲风分类", "曲风", "风格分类", "音乐风格"
            ))
    ));

    /**
     * 页面映射定义
     */
    public static class PageMapping {

        /**
         * 页面名称
         */
        private final String pageName;

        /**
         * 前端路由路径
         */
        private final String pagePath;

        /**
         * 页面可识别别名
         */
        private final List<String> aliases;

        public PageMapping(String pageName, String pagePath, List<String> aliases) {
            this.pageName = pageName;
            this.pagePath = pagePath;
            this.aliases = aliases;
        }

        public String getPageName() {
            return pageName;
        }

        public String getPagePath() {
            return pagePath;
        }

        public List<String> getAliases() {
            return aliases;
        }
    }
}
