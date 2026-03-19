package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.PlaylistBinding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Mapper
public interface PlaylistBindingMapper extends BaseMapper<PlaylistBinding> {

    /** 直接插入绑定关系，绕过 MyBatis-Plus 主键逻辑 */
    @org.apache.ibatis.annotations.Insert("INSERT INTO tb_playlist_binding (playlist_id, song_id) VALUES (#{playlistId}, #{songId})")
    int insertBinding(@org.apache.ibatis.annotations.Param("playlistId") Long playlistId,
                      @org.apache.ibatis.annotations.Param("songId") Long songId);

    /** 删除绑定关系 */
    @org.apache.ibatis.annotations.Delete("DELETE FROM tb_playlist_binding WHERE playlist_id = #{playlistId} AND song_id = #{songId}")
    int deleteBinding(@org.apache.ibatis.annotations.Param("playlistId") Long playlistId,
                      @org.apache.ibatis.annotations.Param("songId") Long songId);

    /** 查询是否已存在 */
    @org.apache.ibatis.annotations.Select("SELECT COUNT(*) FROM tb_playlist_binding WHERE playlist_id = #{playlistId} AND song_id = #{songId}")
    long countBinding(@org.apache.ibatis.annotations.Param("playlistId") Long playlistId,
                      @org.apache.ibatis.annotations.Param("songId") Long songId);
}
