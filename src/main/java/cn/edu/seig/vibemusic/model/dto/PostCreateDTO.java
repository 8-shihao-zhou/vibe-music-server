package cn.edu.seig.vibemusic.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建帖子DTO
 *
 * @author sunpingli
 * @since 2026-02-08
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
     * 帖子分类: SHARE-创作分享, TECH-技术交流, QA-问答互助, CHAT-灌水闲聊
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
     * 状态: 0-草稿, 1-已发布
     */
    private Integer status;
}
