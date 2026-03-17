package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.PostMedia;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子媒体Mapper
 *
 * @author system
 * @since 2026-03-16
 */
@Mapper
public interface PostMediaMapper extends BaseMapper<PostMedia> {

    /**
     * 获取帖子的所有媒体
     *
     * @param postId 帖子ID
     * @return 媒体列表
     */
    @Select("SELECT * FROM tb_post_media WHERE post_id = #{postId} ORDER BY sort_order ASC, id ASC")
    List<PostMedia> selectByPostId(@Param("postId") Long postId);

    /**
     * 获取帖子的指定类型媒体
     *
     * @param postId    帖子ID
     * @param mediaType 媒体类型
     * @return 媒体列表
     */
    @Select("SELECT * FROM tb_post_media WHERE post_id = #{postId} AND media_type = #{mediaType} ORDER BY sort_order ASC, id ASC")
    List<PostMedia> selectByPostIdAndType(@Param("postId") Long postId, @Param("mediaType") Integer mediaType);

    /**
     * 删除帖子的所有媒体
     *
     * @param postId 帖子ID
     * @return 删除数量
     */
    @Delete("DELETE FROM tb_post_media WHERE post_id = #{postId}")
    Integer deleteByPostId(@Param("postId") Long postId);

    /**
     * 批量插入媒体
     *
     * @param mediaList 媒体列表
     * @return 插入数量
     */
    Integer batchInsert(@Param("list") List<PostMedia> mediaList);
}
