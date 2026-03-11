package cn.edu.seig.vibemusic.model.vo;

import lombok.Data;

/**
 * 标签VO
 *
 * @author system
 * @since 2026-03-11
 */
@Data
public class TagVO {
    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 帖子数量
     */
    private Integer postCount;
}
