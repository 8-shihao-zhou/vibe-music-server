package cn.edu.seig.vibemusic.agent.model.vo;

import lombok.Data;

/**
 * Agent 工具执行后返回的结构化数据
 * 这个对象主要用于后端组装 actions，也方便前端调试查看
 */
@Data
public class AgentToolDataVO {

    /**
     * 歌曲相关
     */
    private Long songId;
    private String songName;
    private String artistName;
    private String album;
    private String coverUrl;
    private String audioUrl;

    /**
     * 用户相关
     */
    private Integer unreadCount;
    private Integer availablePoints;
    private Integer totalPoints;

    /**
     * 页面导航相关
     */
    private String pagePath;
    private String pageName;

    /**
     * 是否成功找到目标
     */
    private Boolean success;

    /**
     * 说明信息，便于排查
     */
    private String message;

    /**
     * 站内搜索相关
     */
    private String searchType;
    private String searchKeyword;

}

