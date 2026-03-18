package cn.edu.seig.vibemusic.enums;

import lombok.Getter;

@Getter
public enum PointsActionType {
    DAILY_LOGIN("DAILY_LOGIN", "每日登录", 5, 5, 1),
    POST_CREATE("POST_CREATE", "发布帖子", 10, 10, 1),
    COMMENT_CREATE("COMMENT_CREATE", "发布评论", 2, 6, 3),
    POST_LIKED("POST_LIKED", "帖子被点赞", 1, -1, -1),
    COMMENT_LIKED("COMMENT_LIKED", "评论被点赞", 1, -1, -1),
    FOLLOWED("FOLLOWED", "被关注", 5, -1, -1),
    MV_CREATE("MV_CREATE", "AI创作MV", 15, 45, 3),
    POST_SHARE("POST_SHARE", "分享帖子", 3, 15, 5);

    private final String code;
    private final String description;
    private final Integer points;
    private final Integer dailyLimit; // -1表示无限制
    private final Integer maxCount; // 每日最大次数，-1表示无限制

    PointsActionType(String code, String description, Integer points, Integer dailyLimit, Integer maxCount) {
        this.code = code;
        this.description = description;
        this.points = points;
        this.dailyLimit = dailyLimit;
        this.maxCount = maxCount;
    }

    public static PointsActionType fromCode(String code) {
        for (PointsActionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
