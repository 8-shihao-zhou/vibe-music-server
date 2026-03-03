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
}
