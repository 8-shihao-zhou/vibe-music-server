package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建帖子DTO
 *
 * @author system
 * @since 2026-03-16
 */
@Data
public class PostCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
     * 标签(JSON数组格式)
     */
    private String tags;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 图片URL列表
     */
    private List<String> images;

    /**
     * MV ID（从用户作品库选择）
     */
    private Long mvId;

    /**
     * 状态: 0-草稿, 1-已发布
     */
    private Integer status;
}
