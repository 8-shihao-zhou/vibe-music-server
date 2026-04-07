package cn.edu.seig.vibemusic.controller;


import cn.edu.seig.vibemusic.model.dto.CommentPlaylistDTO;
import cn.edu.seig.vibemusic.model.dto.CommentSongDTO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    /**
     * 新增歌曲评论
     *
     * @param commentSongDTO 评论信息
     * @return 结果
     */
    @PostMapping("/addSongComment")
    public Result addSongComment(@RequestBody CommentSongDTO commentSongDTO) {
        return commentService.addSongComment(commentSongDTO);
    }

    /**
     * 新增歌单评论
     *
     * @param commentPlaylistDTO 评论信息
     * @return 结果
     */
    @PostMapping("/addPlaylistComment")
    public Result addPlaylistComment(@RequestBody CommentPlaylistDTO commentPlaylistDTO) {
        return commentService.addPlaylistComment(commentPlaylistDTO);
    }

    /**
     * 点赞评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @PatchMapping("/likeComment/{id}")
    public Result likeComment(@PathVariable("id") Long commentId) {
        return commentService.likeComment(commentId);
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @PatchMapping("/cancelLikeComment/{id}")
    public Result cancelLikeComment(@PathVariable("id") Long commentId) {
        return commentService.cancelLikeComment(commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @DeleteMapping("/deleteComment/{id}")
    public Result deleteComment(@PathVariable("id") Long commentId) {
        return commentService.deleteComment(commentId);
    }

    /**
     * 管理端分页查询歌曲/歌单评论
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 关键词
     * @param type 评论类型：0-歌曲评论，1-歌单评论
     * @return 评论列表
     */
    @GetMapping("/admin/list")
    public Result getAdminComments(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type) {
        return commentService.getAdminComments(pageNum, pageSize, keyword, type);
    }

    /**
     * 管理端删除评论
     *
     * @param commentId 评论 ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/{id}")
    public Result adminDeleteComment(@PathVariable("id") Long commentId) {
        return commentService.adminDeleteComment(commentId);
    }

}
