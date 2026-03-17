package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.UserMv;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户MV作品Mapper
 *
 * @author system
 * @since 2026-03-16
 */
@Mapper
public interface UserMvMapper extends BaseMapper<UserMv> {

    /**
     * 获取用户的MV列表
     *
     * @param userId 用户ID
     * @param status 状态（null表示所有状态）
     * @return MV列表
     */
    @Select("<script>" +
            "SELECT * FROM tb_user_mv " +
            "WHERE user_id = #{userId} " +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<UserMv> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 检查MV是否属于指定用户
     *
     * @param mvId   MV ID
     * @param userId 用户ID
     * @return 数量
     */
    @Select("SELECT COUNT(*) FROM tb_user_mv WHERE id = #{mvId} AND user_id = #{userId}")
    Integer checkMvOwnership(@Param("mvId") Long mvId, @Param("userId") Long userId);
}
