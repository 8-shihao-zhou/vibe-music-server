package cn.edu.seig.vibemusic.enums;

import lombok.Getter;

@Getter
public enum MallItemType {
    POST_TOP("POST_TOP", "帖子置顶"),
    POST_HIGHLIGHT("POST_HIGHLIGHT", "帖子高亮"),
    AVATAR_FRAME("AVATAR_FRAME", "头像框"),
    NICKNAME_COLOR("NICKNAME_COLOR", "昵称颜色"),
    PROFILE_THEME("PROFILE_THEME", "主页装扮"),
    POST_THEME("POST_THEME", "帖子装扮");

    private final String code;
    private final String description;

    MallItemType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static MallItemType fromCode(String code) {
        for (MallItemType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}