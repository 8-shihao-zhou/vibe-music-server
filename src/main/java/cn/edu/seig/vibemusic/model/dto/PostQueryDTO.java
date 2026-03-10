package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 帖子查询DTO
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Data
public class PostQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类
     */
    private String category;

    /**
     * 关键词搜索（标题或内容）
     */
    private String keyword;

    /**
     * 标签
     */
    private String tag;

    /**
     * 用户ID（查询某个用户的帖子）
     */
    private Long userId;

    /**
     * 是否只查询热门
     */
    private Integer isHot;

    /**
     * 排序方式: latest-最新, hot-最热, view-浏览最多
     */
    private String sortBy;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 帖子状态（管理端使用）: 1-已发布, 0-草稿, -1-已删除
     */
    private Integer status;
}
