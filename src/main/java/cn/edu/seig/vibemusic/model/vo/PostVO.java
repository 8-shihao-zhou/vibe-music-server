package cn.edu.seig.vibemusic.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子VO
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Data
public class PostVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子分类
     */
    private String category;

    /**
     * 标签
     */
    private String tags;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否热门
     */
    private Integer isHot;

    /**
     * 是否高亮（通过积分商城购买）
     */
    private Boolean isHighlight;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已收藏
     */
    private Boolean isFavorited;

    /**
     * 图片数量
     */
    private Integer imageCount;

    /**
     * 是否包含MV
     */
    private Integer hasMv;

    /**
     * 图片列表
     */
    private java.util.List<String> images;

    /**
     * MV信息
     */
    private PostMvVO mv;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
