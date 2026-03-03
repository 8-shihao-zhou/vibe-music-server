package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新帖子DTO
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Data
public class PostUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 帖子ID
     */
    private Long id;

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
     * 状态
     */
    private Integer status;
}
