package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.dto.PostCreateDTO;
import cn.edu.seig.vibemusic.model.dto.PostQueryDTO;
import cn.edu.seig.vibemusic.model.dto.PostUpdateDTO;
import cn.edu.seig.vibemusic.model.entity.CommunityPost;
import cn.edu.seig.vibemusic.model.vo.PostDetailVO;
import cn.edu.seig.vibemusic.model.vo.PostVO;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 社区帖子服务接口
 *
 * @author sunpingli
 * @since 2026-02-08
 */
public interface ICommunityPostService extends IService<CommunityPost> {

    /**
     * 创建帖子
     */
    Result createPost(PostCreateDTO postCreateDTO);

    /**
     * 更新帖子
     */
    Result updatePost(PostUpdateDTO postUpdateDTO);

    /**
     * 删除帖子
     */
    Result deletePost(Long postId);

    /**
     * 分页查询帖子列表
     */
    Result<IPage<PostVO>> getPostList(PostQueryDTO queryDTO, Long currentUserId);

    /**
     * 获取帖子详情
     */
    Result<PostDetailVO> getPostDetail(Long postId, Long currentUserId);

    /**
     * 点赞帖子
     */
    Result likePost(Long postId);

    /**
     * 取消点赞帖子
     */
    Result unlikePost(Long postId);

    /**
     * 评论帖子
     */
    Result commentPost(Long postId, String content, Long parentId);

    /**
     * 删除评论
     */
    Result deleteComment(Long commentId);

    /**
     * 点赞评论
     */
    Result likeComment(Long commentId);

    /**
     * 取消点赞评论
     */
    Result unlikeComment(Long commentId);

    /**
     * 获取用户统计信息
     */
    Result<Map<String, Object>> getUserStats(Long userId);

    /**
     * 收藏帖子
     */
    Result favoritePost(Long postId);

    /**
     * 取消收藏帖子
     */
    Result unfavoritePost(Long postId);

    /**
     * 获取用户收藏列表
     */
    Result<IPage<PostVO>> getUserFavorites(Integer pageNum, Integer pageSize);

    // ==================== 管理端接口 ====================

    /**
     * 管理端 - 分页查询所有帖子（包括草稿和已删除）
     */
    Result<IPage<PostVO>> getAdminPostList(PostQueryDTO queryDTO);

    /**
     * 管理端 - 删除帖子
     */
    Result adminDeletePost(Long postId, Boolean permanent);

    /**
     * 管理端 - 批量删除帖子
     */
    Result adminBatchDeletePosts(java.util.List<Long> postIds, Boolean permanent);

    /**
     * 管理端 - 置顶/取消置顶帖子
     */
    Result adminToggleTop(Long postId, Integer isTop);

    /**
     * 管理端 - 设置/取消热门帖子
     */
    Result adminToggleHot(Long postId, Integer isHot);

    /**
     * 管理端 - 获取统计数据
     */
    Result<java.util.Map<String, Object>> getAdminStatistics();

    /**
     * 管理端 - 查询所有评论
     */
    Result<java.util.Map<String, Object>> getAdminComments(Integer pageNum, Integer pageSize, String keyword);

    /**
     * 管理端 - 删除评论
     */
    Result adminDeleteComment(Long commentId);
}
