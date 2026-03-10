package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.constant.JwtClaimsConstant;
import cn.edu.seig.vibemusic.model.dto.PostCreateDTO;
import cn.edu.seig.vibemusic.model.dto.PostQueryDTO;
import cn.edu.seig.vibemusic.model.dto.PostUpdateDTO;
import cn.edu.seig.vibemusic.model.vo.PostDetailVO;
import cn.edu.seig.vibemusic.model.vo.PostVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.ICommunityPostService;
import cn.edu.seig.vibemusic.util.ThreadLocalUtil;
import cn.edu.seig.vibemusic.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 社区帖子控制器
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@RestController
@RequestMapping("/community/post")
public class CommunityPostController {

    @Autowired
    private ICommunityPostService communityPostService;
    
    @Autowired
    private HttpServletRequest request; // 注入 Request 对象

    /**
     * 创建帖子
     *
     * @param postCreateDTO 帖子信息
     * @return 结果
     */
    @PostMapping("/create")
    public Result createPost(@RequestBody PostCreateDTO postCreateDTO) {
        return communityPostService.createPost(postCreateDTO);
    }

    /**
     * 更新帖子
     *
     * @param postUpdateDTO 帖子信息
     * @return 结果
     */
    @PutMapping("/update")
    public Result updatePost(@RequestBody PostUpdateDTO postUpdateDTO) {
        return communityPostService.updatePost(postUpdateDTO);
    }

    /**
     * 删除帖子
     *
     * @param postId 帖子ID
     * @return 结果
     */
    @DeleteMapping("/delete/{id}")
    public Result deletePost(@PathVariable("id") Long postId) {
        return communityPostService.deletePost(postId);
    }

    /**
     * 分页查询帖子列表
     *
     * @param queryDTO 查询条件
     * @return 帖子列表
     */
    @PostMapping("/list")
    public Result<IPage<PostVO>> getPostList(@RequestBody PostQueryDTO queryDTO) {
        // 在 Controller 层获取当前用户ID
        Long currentUserId = getCurrentUserId();
        System.out.println(">>> [Controller] getPostList() - currentUserId: " + currentUserId);
        return communityPostService.getPostList(queryDTO, currentUserId);
    }

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/detail/{id}")
    public Result<PostDetailVO> getPostDetail(@PathVariable("id") Long postId) {
        // 在 Controller 层获取当前用户ID
        Long currentUserId = getCurrentUserId();
        System.out.println(">>> [Controller] getPostDetail() - postId: " + postId + ", currentUserId: " + currentUserId);
        return communityPostService.getPostDetail(postId, currentUserId);
    }

    /**
     * 获取当前登录用户ID（支持未登录）
     * 从 Request Attribute 获取，避免 ThreadLocal 线程切换问题
     */
    private Long getCurrentUserId() {
        try {
            // 优先从 Request Attribute 获取
            Object userIdObj = request.getAttribute("userId");
            System.out.println(">>> [Controller] getCurrentUserId() - Request Attribute userId: " + userIdObj);
            
            if (userIdObj != null) {
                Long userId = TypeConversionUtil.toLong(userIdObj);
                System.out.println(">>> [Controller] getCurrentUserId() - 从 Request 获取到 userId: " + userId);
                return userId;
            }
            
            // 如果 Request Attribute 为空，尝试从 ThreadLocal 获取（兼容性）
            Map<String, Object> map = ThreadLocalUtil.get();
            System.out.println(">>> [Controller] getCurrentUserId() - ThreadLocal map: " + map);
            if (map == null || map.isEmpty()) {
                System.out.println(">>> [Controller] getCurrentUserId() - map is null or empty");
                return null; // 未登录返回null
            }
            userIdObj = map.get(JwtClaimsConstant.USER_ID);
            System.out.println(">>> [Controller] getCurrentUserId() - 从 ThreadLocal 获取到 userIdObj: " + userIdObj);
            Long userId = TypeConversionUtil.toLong(userIdObj);
            System.out.println(">>> [Controller] getCurrentUserId() - userId: " + userId);
            return userId;
        } catch (Exception e) {
            System.out.println(">>> [Controller] getCurrentUserId() - exception: " + e.getMessage());
            e.printStackTrace();
            return null; // 出错时返回null
        }
    }

    /**
     * 点赞帖子
     *
     * @param postId 帖子ID
     * @return 结果
     */
    @PostMapping("/like/{id}")
    public Result likePost(@PathVariable("id") Long postId) {
        return communityPostService.likePost(postId);
    }

    /**
     * 取消点赞帖子
     *
     * @param postId 帖子ID
     * @return 结果
     */
    @DeleteMapping("/like/{id}")
    public Result unlikePost(@PathVariable("id") Long postId) {
        return communityPostService.unlikePost(postId);
    }

    /**
     * 评论帖子
     *
     * @param postId 帖子ID
     * @param content 评论内容
     * @param parentId 父评论ID（可选）
     * @return 结果
     */
    @PostMapping("/comment/{id}")
    public Result commentPost(@PathVariable("id") Long postId,
                              @RequestParam String content,
                              @RequestParam(required = false) Long parentId) {
        return communityPostService.commentPost(postId, content, parentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 结果
     */
    @DeleteMapping("/comment/{id}")
    public Result deleteComment(@PathVariable("id") Long commentId) {
        return communityPostService.deleteComment(commentId);
    }

    /**
     * 点赞评论
     *
     * @param commentId 评论ID
     * @return 结果
     */
    @PostMapping("/comment/like/{id}")
    public Result likeComment(@PathVariable("id") Long commentId) {
        return communityPostService.likeComment(commentId);
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论ID
     * @return 结果
     */
    @DeleteMapping("/comment/like/{id}")
    public Result unlikeComment(@PathVariable("id") Long commentId) {
        return communityPostService.unlikeComment(commentId);
    }

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 用户统计信息
     */
    @GetMapping("/user/stats/{userId}")
    public Result<Map<String, Object>> getUserStats(@PathVariable("userId") Long userId) {
        return communityPostService.getUserStats(userId);
    }

    /**
     * 收藏帖子
     *
     * @param postId 帖子ID
     * @return 结果
     */
    @PostMapping("/favorite/{id}")
    public Result favoritePost(@PathVariable("id") Long postId) {
        return communityPostService.favoritePost(postId);
    }

    /**
     * 取消收藏帖子
     *
     * @param postId 帖子ID
     * @return 结果
     */
    @DeleteMapping("/favorite/{id}")
    public Result unfavoritePost(@PathVariable("id") Long postId) {
        return communityPostService.unfavoritePost(postId);
    }

    /**
     * 获取用户收藏列表
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    @GetMapping("/favorites")
    public Result<IPage<PostVO>> getUserFavorites(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        return communityPostService.getUserFavorites(pageNum, pageSize);
    }

    // ==================== 管理端接口 ====================

    /**
     * 管理端 - 分页查询所有帖子（包括草稿和已删除）
     *
     * @param queryDTO 查询条件
     * @return 帖子列表
     */
    @PostMapping("/admin/list")
    public Result<IPage<PostVO>> getAdminPostList(@RequestBody PostQueryDTO queryDTO) {
        System.out.println(">>> [Admin] getAdminPostList() - 管理端查询帖子列表");
        return communityPostService.getAdminPostList(queryDTO);
    }

    /**
     * 管理端 - 删除帖子（物理删除或软删除）
     *
     * @param postId 帖子ID
     * @param permanent 是否永久删除（true-物理删除，false-软删除）
     * @return 结果
     */
    @DeleteMapping("/admin/delete/{id}")
    public Result adminDeletePost(@PathVariable("id") Long postId,
                                  @RequestParam(defaultValue = "false") Boolean permanent) {
        System.out.println(">>> [Admin] adminDeletePost() - postId: " + postId + ", permanent: " + permanent);
        return communityPostService.adminDeletePost(postId, permanent);
    }

    /**
     * 管理端 - 批量删除帖子
     *
     * @param postIds 帖子ID列表
     * @param permanent 是否永久删除
     * @return 结果
     */
    @PostMapping("/admin/batch-delete")
    public Result adminBatchDeletePosts(@RequestBody java.util.List<Long> postIds,
                                       @RequestParam(defaultValue = "false") Boolean permanent) {
        System.out.println(">>> [Admin] adminBatchDeletePosts() - count: " + postIds.size() + ", permanent: " + permanent);
        return communityPostService.adminBatchDeletePosts(postIds, permanent);
    }

    /**
     * 管理端 - 置顶/取消置顶帖子
     *
     * @param postId 帖子ID
     * @param isTop 是否置顶（1-置顶，0-取消置顶）
     * @return 结果
     */
    @PutMapping("/admin/top/{id}")
    public Result adminToggleTop(@PathVariable("id") Long postId,
                                 @RequestParam Integer isTop) {
        System.out.println(">>> [Admin] adminToggleTop() - postId: " + postId + ", isTop: " + isTop);
        return communityPostService.adminToggleTop(postId, isTop);
    }

    /**
     * 管理端 - 设置/取消热门帖子
     *
     * @param postId 帖子ID
     * @param isHot 是否热门（1-热门，0-取消热门）
     * @return 结果
     */
    @PutMapping("/admin/hot/{id}")
    public Result adminToggleHot(@PathVariable("id") Long postId,
                                @RequestParam Integer isHot) {
        System.out.println(">>> [Admin] adminToggleHot() - postId: " + postId + ", isHot: " + isHot);
        return communityPostService.adminToggleHot(postId, isHot);
    }

    /**
     * 管理端 - 获取社区统计数据
     *
     * @return 统计数据
     */
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getAdminStatistics() {
        System.out.println(">>> [Admin] getAdminStatistics() - 获取统计数据");
        return communityPostService.getAdminStatistics();
    }

    /**
     * 管理端 - 查询所有评论（分页）
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 关键词（可选）
     * @return 评论列表
     */
    @GetMapping("/admin/comments")
    public Result<Map<String, Object>> getAdminComments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        System.out.println(">>> [Admin] getAdminComments() - pageNum: " + pageNum + ", pageSize: " + pageSize);
        return communityPostService.getAdminComments(pageNum, pageSize, keyword);
    }

    /**
     * 管理端 - 删除评论
     *
     * @param commentId 评论ID
     * @return 结果
     */
    @DeleteMapping("/admin/comment/{id}")
    public Result adminDeleteComment(@PathVariable("id") Long commentId) {
        System.out.println(">>> [Admin] adminDeleteComment() - commentId: " + commentId);
        return communityPostService.adminDeleteComment(commentId);
    }
}
