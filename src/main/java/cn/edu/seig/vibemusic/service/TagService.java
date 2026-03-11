package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.vo.TagVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author system
 * @since 2026-03-11
 */
public interface TagService {

    /**
     * 获取热门标签列表
     *
     * @param limit 返回数量限制
     * @return 热门标签列表
     */
    List<TagVO> getHotTags(Integer limit);

    /**
     * 搜索标签
     *
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 标签分页列表
     */
    Page<TagVO> searchTags(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 更新帖子标签（创建或更新帖子时调用）
     *
     * @param postId 帖子ID
     * @param tags 标签列表（JSON数组字符串）
     */
    void updatePostTags(Long postId, String tags);

    /**
     * 删除帖子标签（删除帖子时调用）
     *
     * @param postId 帖子ID
     */
    void deletePostTags(Long postId);
}
