package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.CommunityPost;
import cn.edu.seig.vibemusic.model.vo.PostVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 社区帖子 Mapper 接口
 *
 * @author sunpingli
 * @since 2026-02-08
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {

    /**
     * 分页查询帖子列表（带用户信息）
     */
    IPage<PostVO> selectPostPage(Page<PostVO> page, 
                                  @Param("category") String category,
                                  @Param("keyword") String keyword,
                                  @Param("tag") String tag,
                                  @Param("userId") Long userId,
                                  @Param("isHot") Integer isHot,
                                  @Param("status") Integer status,
                                  @Param("sortBy") String sortBy,
                                  @Param("currentUserId") Long currentUserId);

    /**
     * 查询帖子详情（带用户信息）
     */
    PostVO selectPostDetail(@Param("postId") Long postId, 
                           @Param("currentUserId") Long currentUserId);

    /**
     * 统计用户所有帖子的总获赞数
     */
    Long countUserTotalLikes(@Param("userId") Long userId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(@Param("postId") Long postId);

    /**
     * 增加评论数
     */
    void incrementCommentCount(@Param("postId") Long postId);

    /**
     * 减少评论数
     */
    void decrementCommentCount(@Param("postId") Long postId);

    /**
     * 管理端 - 分页查询所有帖子（包括草稿和已删除）
     */
    IPage<PostVO> selectAdminPostPage(Page<PostVO> page,
                                      @Param("category") String category,
                                      @Param("keyword") String keyword,
                                      @Param("tag") String tag,
                                      @Param("userId") Long userId,
                                      @Param("status") Integer status);
}
