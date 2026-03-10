package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constant.JwtClaimsConstant;
import cn.edu.seig.vibemusic.constant.MessageConstant;
import cn.edu.seig.vibemusic.mapper.CommentLikeMapper;
import cn.edu.seig.vibemusic.mapper.CommentMapper;
import cn.edu.seig.vibemusic.mapper.CommunityPostMapper;
import cn.edu.seig.vibemusic.mapper.PostFavoriteMapper;
import cn.edu.seig.vibemusic.mapper.PostLikeMapper;
import cn.edu.seig.vibemusic.mapper.UserMapper;
import cn.edu.seig.vibemusic.model.dto.PostCreateDTO;
import cn.edu.seig.vibemusic.model.dto.PostQueryDTO;
import cn.edu.seig.vibemusic.model.dto.PostUpdateDTO;
import cn.edu.seig.vibemusic.model.entity.Comment;
import cn.edu.seig.vibemusic.model.entity.CommentLike;
import cn.edu.seig.vibemusic.model.entity.CommunityPost;
import cn.edu.seig.vibemusic.model.entity.PostFavorite;
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
import java.util.HashMap;
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

    @Autowired
    private PostFavoriteMapper postFavoriteMapper;

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
        // 返回新创建的帖子ID
        return Result.success(post.getId());
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

        // 软删除帖子
        post.setStatus(-1); // 状态设为已删除
        post.setUpdateTime(LocalDateTime.now());
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
                queryDTO.getStatus(),
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

    /**
     * 点赞评论
     */
    @Override
    @Transactional
    public Result likeComment(Long commentId) {
        Long userId = getRequiredUserId();

        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        // 检查是否已经点赞
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
               .eq(CommentLike::getUserId, userId);
        CommentLike existingLike = commentLikeMapper.selectOne(wrapper);
        
        if (existingLike != null) {
            return Result.error("已经点赞过了");
        }

        // 创建点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);
        commentLike.setCreateTime(LocalDateTime.now());
        
        if (commentLikeMapper.insert(commentLike) == 0) {
            return Result.error("点赞失败");
        }

        // 增加评论点赞数
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentMapper.updateById(comment);

        return Result.success("点赞成功");
    }

    /**
     * 取消点赞评论
     */
    @Override
    @Transactional
    public Result unlikeComment(Long commentId) {
        Long userId = getRequiredUserId();

        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        // 查找点赞记录
        LambdaQueryWrapper<CommentLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentLike::getCommentId, commentId)
               .eq(CommentLike::getUserId, userId);
        CommentLike commentLike = commentLikeMapper.selectOne(wrapper);
        
        if (commentLike == null) {
            return Result.error("还未点赞");
        }

        // 删除点赞记录
        if (commentLikeMapper.deleteById(commentLike.getId()) == 0) {
            return Result.error("取消点赞失败");
        }

        // 减少评论点赞数
        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentMapper.updateById(comment);
        }

        return Result.success("取消点赞成功");
    }

    /**
     * 获取用户统计信息
     */
    @Override
    public Result<Map<String, Object>> getUserStats(Long userId) {
        System.out.println(">>> [getUserStats] 开始查询用户统计，userId: " + userId);
        
        // 查询用户信息
        User user = userMapper.selectById(userId);
        System.out.println(">>> [getUserStats] 查询用户结果: " + (user != null ? user.getUsername() : "null"));
        
        if (user == null) {
            System.out.println(">>> [getUserStats] 用户不存在，userId: " + userId);
            return Result.error("用户不存在");
        }

        Map<String, Object> stats = new HashMap<>();
        
        // 用户基本信息
        stats.put("userId", user.getUserId());
        stats.put("username", user.getUsername());
        stats.put("userAvatar", user.getUserAvatar());
        
        // 统计帖子数（只统计已发布的）
        LambdaQueryWrapper<CommunityPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.eq(CommunityPost::getUserId, userId)
                   .eq(CommunityPost::getStatus, 1);
        Long postCount = communityPostMapper.selectCount(postWrapper);
        stats.put("postCount", postCount);
        
        // 统计获赞数（所有帖子的点赞总数）
        Long totalLikes = communityPostMapper.countUserTotalLikes(userId);
        stats.put("totalLikes", totalLikes != null ? totalLikes : 0);
        
        // 统计评论数（用户发表的评论数）
        LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
        commentWrapper.eq(Comment::getUserId, userId)
                     .eq(Comment::getCommentType, 3); // 社区帖子评论
        Long commentCount = commentMapper.selectCount(commentWrapper);
        stats.put("commentCount", commentCount);
        
        return Result.success(stats);
    }

    /**
     * 收藏帖子
     */
    @Override
    @Transactional
    public Result favoritePost(Long postId) {
        Long userId = getRequiredUserId();

        // 检查是否已收藏
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getUserId, userId);
        PostFavorite existingFavorite = postFavoriteMapper.selectOne(wrapper);

        if (existingFavorite != null) {
            return Result.error("已经收藏过了");
        }

        // 添加收藏记录
        PostFavorite postFavorite = new PostFavorite();
        postFavorite.setPostId(postId);
        postFavorite.setUserId(userId);
        postFavorite.setCreateTime(LocalDateTime.now());

        if (postFavoriteMapper.insert(postFavorite) == 0) {
            return Result.error("收藏失败");
        }

        // 更新帖子收藏数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null) {
            post.setFavoriteCount(post.getFavoriteCount() != null ? post.getFavoriteCount() + 1 : 1);
            communityPostMapper.updateById(post);
        }

        return Result.success("收藏成功");
    }

    /**
     * 取消收藏帖子
     */
    @Override
    @Transactional
    public Result unfavoritePost(Long postId) {
        Long userId = getRequiredUserId();

        // 查找收藏记录
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostFavorite::getPostId, postId)
                .eq(PostFavorite::getUserId, userId);
        PostFavorite postFavorite = postFavoriteMapper.selectOne(wrapper);

        if (postFavorite == null) {
            return Result.error("未收藏过");
        }

        // 删除收藏记录
        if (postFavoriteMapper.deleteById(postFavorite.getId()) == 0) {
            return Result.error("取消收藏失败");
        }

        // 更新帖子收藏数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null && post.getFavoriteCount() != null && post.getFavoriteCount() > 0) {
            post.setFavoriteCount(post.getFavoriteCount() - 1);
            communityPostMapper.updateById(post);
        }

        return Result.success("取消收藏成功");
    }

    /**
     * 获取用户收藏列表
     */
    @Override
    public Result<IPage<PostVO>> getUserFavorites(Integer pageNum, Integer pageSize) {
        Long userId = getRequiredUserId();

        // 查询用户收藏的帖子ID列表
        LambdaQueryWrapper<PostFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostFavorite::getUserId, userId)
                .orderByDesc(PostFavorite::getCreateTime);
        
        Page<PostFavorite> favoritePage = new Page<>(pageNum, pageSize);
        IPage<PostFavorite> favoriteResult = postFavoriteMapper.selectPage(favoritePage, wrapper);

        if (favoriteResult.getRecords().isEmpty()) {
            Page<PostVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setTotal(0);
            return Result.success(emptyPage);
        }

        // 获取帖子ID列表（保持收藏顺序）
        List<Long> postIds = favoriteResult.getRecords().stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toList());

        // 查询帖子详情
        LambdaQueryWrapper<CommunityPost> postWrapper = new LambdaQueryWrapper<>();
        postWrapper.in(CommunityPost::getId, postIds)
                .eq(CommunityPost::getStatus, 1); // 只查询已发布的帖子
        List<CommunityPost> posts = communityPostMapper.selectList(postWrapper);

        // 创建帖子ID到帖子的映射
        Map<Long, CommunityPost> postMap = posts.stream()
                .collect(Collectors.toMap(CommunityPost::getId, post -> post));

        // 按照收藏顺序转换为VO
        List<PostVO> postVOList = new ArrayList<>();
        for (Long postId : postIds) {
            CommunityPost post = postMap.get(postId);
            if (post == null) {
                continue; // 帖子可能已被删除
            }
            
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);
            
            // 查询用户信息
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setUserAvatar(user.getUserAvatar());
            }
            
            // 检查点赞状态
            Integer likeCount = postLikeMapper.checkUserLike(post.getId(), userId);
            vo.setIsLiked(likeCount != null && likeCount > 0);
            
            // 已收藏
            vo.setIsFavorited(true);
            
            postVOList.add(vo);
        }

        // 构建分页结果
        Page<PostVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setRecords(postVOList);
        resultPage.setTotal(favoriteResult.getTotal());

        return Result.success(resultPage);
    }

    // ==================== 管理端方法实现 ====================

    /**
     * 管理端 - 分页查询所有帖子（包括草稿和已删除）
     */
    @Override
    public Result<IPage<PostVO>> getAdminPostList(PostQueryDTO queryDTO) {
        try {
            // 构建分页对象
            Page<PostVO> page = new Page<>(
                    queryDTO.getPageNum() != null ? queryDTO.getPageNum() : 1,
                    queryDTO.getPageSize() != null ? queryDTO.getPageSize() : 10
            );

            // 管理端查询所有状态的帖子（包括草稿status=0和已删除status=-1）
            IPage<PostVO> result = communityPostMapper.selectAdminPostPage(
                    page,
                    queryDTO.getCategory(),
                    queryDTO.getKeyword(),
                    queryDTO.getTag(),
                    queryDTO.getUserId(),
                    queryDTO.getStatus() // 管理端可以指定状态查询
            );

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 管理端 - 删除帖子
     */
    @Override
    @Transactional
    public Result adminDeletePost(Long postId, Boolean permanent) {
        try {
            CommunityPost post = communityPostMapper.selectById(postId);
            if (post == null) {
                return Result.error("帖子不存在");
            }

            if (permanent) {
                // 物理删除：删除帖子及相关数据
                // 1. 删除帖子的所有点赞
                LambdaQueryWrapper<PostLike> likeWrapper = new LambdaQueryWrapper<>();
                likeWrapper.eq(PostLike::getPostId, postId);
                postLikeMapper.delete(likeWrapper);

                // 2. 删除帖子的所有评论
                LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
                commentWrapper.eq(Comment::getCommentType, 3); // 社区评论
                commentWrapper.eq(Comment::getTargetId, postId);
                commentMapper.delete(commentWrapper);

                // 3. 删除帖子
                communityPostMapper.deleteById(postId);
                
                return Result.success("永久删除成功");
            } else {
                // 软删除：设置status为-1
                post.setStatus(-1);
                post.setUpdateTime(LocalDateTime.now());
                communityPostMapper.updateById(post);
                
                return Result.success("删除成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 管理端 - 批量删除帖子
     */
    @Override
    @Transactional
    public Result adminBatchDeletePosts(List<Long> postIds, Boolean permanent) {
        try {
            if (postIds == null || postIds.isEmpty()) {
                return Result.error("请选择要删除的帖子");
            }

            int successCount = 0;
            for (Long postId : postIds) {
                Result result = adminDeletePost(postId, permanent);
                if (result.getCode() == 0) {
                    successCount++;
                }
            }

            return Result.success("成功删除 " + successCount + " 个帖子");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 管理端 - 置顶/取消置顶帖子
     */
    @Override
    @Transactional
    public Result adminToggleTop(Long postId, Integer isTop) {
        try {
            CommunityPost post = communityPostMapper.selectById(postId);
            if (post == null) {
                return Result.error("帖子不存在");
            }

            post.setIsTop(isTop);
            post.setUpdateTime(LocalDateTime.now());
            communityPostMapper.updateById(post);

            return Result.success(isTop == 1 ? "置顶成功" : "取消置顶成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 管理端 - 设置/取消热门帖子
     */
    @Override
    @Transactional
    public Result adminToggleHot(Long postId, Integer isHot) {
        try {
            CommunityPost post = communityPostMapper.selectById(postId);
            if (post == null) {
                return Result.error("帖子不存在");
            }

            post.setIsHot(isHot);
            post.setUpdateTime(LocalDateTime.now());
            communityPostMapper.updateById(post);

            return Result.success(isHot == 1 ? "设置热门成功" : "取消热门成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 管理端 - 获取统计数据
     */
    @Override
    public Result<Map<String, Object>> getAdminStatistics() {
        try {
            Map<String, Object> statistics = new java.util.HashMap<>();

            // 总帖子数（不包括已删除）
            LambdaQueryWrapper<CommunityPost> postWrapper = new LambdaQueryWrapper<>();
            postWrapper.ne(CommunityPost::getStatus, -1);
            long totalPosts = communityPostMapper.selectCount(postWrapper);

            // 已发布帖子数
            LambdaQueryWrapper<CommunityPost> publishedWrapper = new LambdaQueryWrapper<>();
            publishedWrapper.eq(CommunityPost::getStatus, 1);
            long publishedPosts = communityPostMapper.selectCount(publishedWrapper);

            // 草稿数
            LambdaQueryWrapper<CommunityPost> draftWrapper = new LambdaQueryWrapper<>();
            draftWrapper.eq(CommunityPost::getStatus, 0);
            long draftPosts = communityPostMapper.selectCount(draftWrapper);

            // 已删除帖子数
            LambdaQueryWrapper<CommunityPost> deletedWrapper = new LambdaQueryWrapper<>();
            deletedWrapper.eq(CommunityPost::getStatus, -1);
            long deletedPosts = communityPostMapper.selectCount(deletedWrapper);

            // 总评论数
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(Comment::getCommentType, 3); // 社区评论
            long totalComments = commentMapper.selectCount(commentWrapper);

            // 总点赞数
            long totalLikes = postLikeMapper.selectCount(null);

            // 总浏览数
            List<CommunityPost> allPosts = communityPostMapper.selectList(null);
            long totalViews = allPosts.stream()
                    .mapToLong(CommunityPost::getViewCount)
                    .sum();

            statistics.put("totalPosts", totalPosts);
            statistics.put("publishedPosts", publishedPosts);
            statistics.put("draftPosts", draftPosts);
            statistics.put("deletedPosts", deletedPosts);
            statistics.put("totalComments", totalComments);
            statistics.put("totalLikes", totalLikes);
            statistics.put("totalViews", totalViews);

            return Result.success(statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 管理端 - 查询所有评论
     */
    @Override
    public Result<Map<String, Object>> getAdminComments(Integer pageNum, Integer pageSize, String keyword) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getCommentType, 3); // 社区评论
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like(Comment::getContent, keyword);
            }
            
            wrapper.orderByDesc(Comment::getCreateTime);

            // 分页查询
            Page<Comment> page = new Page<>(pageNum, pageSize);
            IPage<Comment> result = commentMapper.selectPage(page, wrapper);

            // 构建返回数据
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("records", result.getRecords());
            data.put("total", result.getTotal());
            data.put("pageNum", pageNum);
            data.put("pageSize", pageSize);

            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询评论失败：" + e.getMessage());
        }
    }

    /**
     * 管理端 - 删除评论（无权限限制）
     */
    @Override
    @Transactional
    public Result adminDeleteComment(Long commentId) {
        try {
            Comment comment = commentMapper.selectById(commentId);
            if (comment == null) {
                return Result.error("评论不存在");
            }

            // 删除评论
            commentMapper.deleteById(commentId);

            // 减少帖子评论数
            if (comment.getTargetId() != null) {
                communityPostMapper.decrementCommentCount(comment.getTargetId());
            }

            // 删除该评论的所有回复
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getParentId, commentId);
            List<Comment> replies = commentMapper.selectList(wrapper);
            if (replies != null && !replies.isEmpty()) {
                for (Comment reply : replies) {
                    commentMapper.deleteById(reply.getCommentId());
                    if (comment.getTargetId() != null) {
                        communityPostMapper.decrementCommentCount(comment.getTargetId());
                    }
                }
            }

            return Result.success("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}
