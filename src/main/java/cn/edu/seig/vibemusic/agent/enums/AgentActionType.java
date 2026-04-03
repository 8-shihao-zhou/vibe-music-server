package cn.edu.seig.vibemusic.agent.enums;

import lombok.Getter;

/**
 * Agent 可下发给前端的动作类型
 */
@Getter
public enum AgentActionType {

    /**
     * 仅回复文本，不执行前端动作
     */
    REPLY_ONLY("reply_only"),

    /**
     * 播放歌曲
     */
    PLAY_SONG("play_song"),

    /**
     * 页面跳转
     */
    NAVIGATE_TO("navigate_to"),

    /**
     * 站内搜索
     */
    SEARCH_SITE("search_site");



    private final String code;

    AgentActionType(String code) {
        this.code = code;
    }
}


