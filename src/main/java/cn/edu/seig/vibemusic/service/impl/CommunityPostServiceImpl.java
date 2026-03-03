package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constant.JwtClaimsConstant;
import cn.edu.seig.vibemusic.constant.MessageConstant;
import cn.edu.seig.vibemusic.mapper.CommentLikeMapper;
import cn.edu.seig.vibemusic.mapper.CommentMapper;
import cn.edu.seig.vibemusic.mapper.CommunityPostMapper;
import cn.edu.seig.vibemusic.mapper.PostLikeMapper;
import cn.edu.seig.vibemusic.mapper.UserMapper;
import cn.edu.seig.vibemusic.model.dto.PostCreateDTO;
import cn.edu.seig.vibemusic.model.dto.PostQueryDTO;
import cn.edu.seig.vibemusic.model.dto.PostUpdateDTO;
import cn.edu.seig.vibemusic.model.entity.Comment;
import cn.edu.seig.vibemusic.model.entity.CommentLike;
import cn.edu.seig.vibemusic.model.entity.CommunityPost;
import cn.edu.seig.vibemusic.model.entity.PostLike;
import cn.edu.seig.vibemusic.model.entity.User;
import cn.edu.seig.vibemusic.model.vo.PostCommentVO;
import cn.edu.seig.vibemusic.model.vo.PostDetailVO;
import cn.edu.seig.vibemusic.model.vo.PostVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.ICommunityPostService;
import cn.edu.seig.vibemusic.util.ThreadLocalUtil;
import cn.edu.seig.vibemusic.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区帖子服务实现类
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Service
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements ICommunityPostService {

    @Autowired
    private CommunityPostMapper communityPostMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前登录用户ID（必须登录）
     */
    private Long getRequiredUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        return TypeConversionUtil.toLong(userIdObj);
    }

    /**
     * 创建帖子
     */
    @Override
    @Transactional
    public Result createPost(PostCreateDTO postCreateDTO) {
        Long userId = getRequiredUserId();

        CommunityPost post = new CommunityPost();
        BeanUtils.copyProperties(postCreateDTO, post);
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(0);
        post.setIsHot(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());

        if (communityPostMapper.insert(post) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 更新帖子
     */
    @Override
    @Transactional
    public Result updatePost(PostUpdateDTO postUpdateDTO) {
        Long userId = getRequiredUserId();

        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postUpdateDTO.getId());
        if (post == null) {
            return Result.error("帖子不存在");
        }

        // 检查是否是作者
        if (!post.getUserId().equals(userId)) {
            return Result.error("无权限修改此帖子");
        }

        BeanUtils.copyProperties(postUpdateDTO, post);
        post.setUpdateTime(LocalDateTime.now());

        if (communityPostMapper.updateById(post) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除帖子
     */
    @Override
    @Transactional
    public Result deletePost(Long postId) {
        Long userId = getRequiredUserId();

        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        // 检查是否是作者
        if (!post.getUserId().equals(userId)) {
            return Result.error("无权限删除此帖子");
        }

        // 删除帖子
        post.setStatus(2); // 状态设为已删除
        if (communityPostMapper.updateById(post) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 分页查询帖子列表
     */
    @Override
    public Result<IPage<PostVO>> getPostList(PostQueryDTO queryDTO, Long currentUserId) {
        // 设置默认值
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        if (queryDTO.getSortBy() == null) {
            queryDTO.setSortBy("latest");
        }

        Page<PostVO> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<PostVO> postPage = communityPostMapper.selectPostPage(
                page,
                queryDTO.getCategory(),
                queryDTO.getKeyword(),
                queryDTO.getTag(),
                queryDTO.getUserId(),
                queryDTO.getIsHot(),
                queryDTO.getSortBy(),
                currentUserId // 从Controller传入
        );

        return Result.success(postPage);
    }

    /**
     * 获取帖子详情
     */
    @Override
    @Transactional
    public Result<PostDetailVO> getPostDetail(Long postId, Long currentUserId) {
        System.out.println(">>> [Service] getPostDetail() - 开始获取帖子详情，postId: " + postId);
        System.out.println(">>> [Service] getPostDetail() - currentUserId: " + currentUserId);

        // 增加浏览次数
        communityPostMapper.incrementViewCount(postId);

        // 查询帖子详情
        System.out.println(">>> [Service] getPostDetail() - 调用 selectPostDetail，postId: " + postId + ", currentUserId: " + currentUserId);
        PostVO postVO = communityPostMapper.selectPostDetail(postId, currentUserId);
        if (postVO == null) {
            return Result.error("帖子不存在");
        }

        // 构建详情VO
        PostDetailVO detailVO = new PostDetailVO();
        BeanUtils.copyProperties(postVO, detailVO);

        // 查询评论列表
        List<PostCommentVO> comments = getPostComments(postId, currentUserId);
        detailVO.setComments(comments);

        return Result.success(detailVO);
    }


    /**
     * 获取帖子评论列表（包含回复）
     */
    private List<PostCommentVO> getPostComments(Long postId, Long currentUserId) {
        System.out.println(">>> [评论查询] getPostComments() - postId: " + postId + ", currentUserId: " + currentUserId);
        
        // 查询所有评论
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getCommentType, 3) // 社区帖子评论
                .eq(Comment::getTargetId, postId)
                .orderByAsc(Comment::getCreateTime);
        List<Comment> allComments = commentMapper.selectList(wrapper);

        System.out.println(">>> [评论查询] 查询到评论数量: " + (allComments != null ? allComments.size() : 0));
        
        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为VO并构建树形结构
        Map<Long, PostCommentVO> commentMap = allComments.stream()
                .collect(Collectors.toMap(
                        Comment::getCommentId,
                        comment -> {
                            PostCommentVO vo = new PostCommentVO();
                            vo.setCommentId(comment.getCommentId());
                            vo.setUserId(comment.getUserId());
                            vo.setContent(comment.getContent());
                            vo.setParentId(comment.getParentId());
                            vo.setLikeCount(comment.getLikeCount());
                            vo.setCreateTime(comment.getCreateTime());
                            vo.setReplies(new ArrayList<>());
                            
                            // 查询用户信息
                            User user = userMapper.selectById(comment.getUserId());
                            if (user != null) {
                                vo.setUsername(user.getUsername());
                                vo.setUserAvatar(user.getUserAvatar());
                            }
                            
                            // 检查当前用户是否已点赞（未登录时默认未点赞）
                            if (currentUserId != null) {
                                Integer likeCount = commentLikeMapper.checkUserLike(comment.getCommentId(), currentUserId);
                                vo.setIsLiked(likeCount != null && likeCount > 0);
                            } else {
                                vo.setIsLiked(false);
                            }
                            
                            return vo;
                        }
                ));

        // 构建树形结构
        List<PostCommentVO> rootComments = new ArrayList<>();
        for (PostCommentVO comment : commentMap.values()) {
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                rootComments.add(comment);
            } else {
                PostCommentVO parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getReplies().add(comment);
                }
            }
        }

        return rootComments;
    }

    /**
     * 点赞帖子
     */
    @Override
    @Transactional
    public Result likePost(Long postId) {
        Long userId = getRequiredUserId();

        // 检查是否已点赞
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        PostLike existingLike = postLikeMapper.selectOne(wrapper);

        if (existingLike != null) {
            return Result.error("已经点赞过了");
        }

        // 添加点赞记录
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLike.setCreateTime(LocalDateTime.now());

        if (postLikeMapper.insert(postLike) == 0) {
            return Result.error("点赞失败");
        }

        // 更新帖子点赞数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            communityPostMapper.updateById(post);
        }

        return Result.success("点赞成功");
    }

    /**
     * 取消点赞帖子
     */
    @Override
    @Transactional
    public Result unlikePost(Long postId) {
        Long userId = getRequiredUserId();

        // 查找点赞记录
        LambdaQueryWrapper<PostLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostLike::getPostId, postId)
                .eq(PostLike::getUserId, userId);
        PostLike postLike = postLikeMapper.selectOne(wrapper);

        if (postLike == null) {
            return Result.error("未点赞过");
        }

        // 删除点赞记录
        if (postLikeMapper.deleteById(postLike.getId()) == 0) {
            return Result.error("取消点赞失败");
        }

        // 更新帖子点赞数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null && post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            communityPostMapper.updateById(post);
        }

        return Result.success("取消点赞成功");
    }

    /**
     * 评论帖子
     */
    @Override
    @Transactional
    public Result commentPost(Long postId, String content, Long parentId) {
        System.out.println(">>> [评论] commentPost() - 开始");
        System.out.println(">>> [评论] postId: " + postId);
        System.out.println(">>> [评论] content: " + content);
        System.out.println(">>> [评论] parentId: " + parentId);
        
        Long userId = getRequiredUserId();
        System.out.println(">>> [评论] userId: " + userId);

        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            System.out.println(">>> [评论] 帖子不存在");
            return Result.error("帖子不存在");
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setType(3); // 兼容旧字段
        comment.setCommentType(3); // 社区帖子评论
        comment.setTargetId(postId);
        comment.setContent(content);
        comment.setParentId(parentId == null ? 0L : parentId);
        comment.setLikeCount(0L);
        comment.setCreateTime(LocalDateTime.now());

        System.out.println(">>> [评论] 准备插入评论: " + comment);
        int insertResult = commentMapper.insert(comment);
        System.out.println(">>> [评论] 插入结果: " + insertResult);
        
        if (insertResult == 0) {
            System.out.println(">>> [评论] 评论插入失败");
            return Result.error("评论失败");
        }

        // 增加帖子评论数
        System.out.println(">>> [评论] 增加帖子评论数");
        communityPostMapper.incrementCommentCount(postId);

        System.out.println(">>> [评论] 评论成功");
        return Result.success("评论成功");
    }

    /**
     * 删除评论
     */
    @Override
    @Transactional
    public Result deleteComment(Long commentId) {
        Long userId = getRequiredUserId();

        // 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        // 检查是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            return Result.error("无权限删除此评论");
        }

        // 删除评论
        if (commentMapper.deleteById(commentId) == 0) {
            return Result.error("删除失败");
        }

        // 减少帖子评论数
        communityPostMapper.decrementCommentCount(comment.getTargetId());

        // 删除该评论的所有回复
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        List<Comment> replies = commentMapper.selectList(wrapper);
        if (replies != null && !replies.isEmpty()) {
            for (Comment reply : replies) {
                commentMapper.deleteById(reply.getCommentId());
                communityPostMapper.decrementCommentCount(comment.getTargetId());
            }
        }

        return Result.success("删除成功");
    }
}
