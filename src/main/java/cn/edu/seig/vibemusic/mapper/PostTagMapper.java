package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.PostTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子标签关联Mapper接口
 *
 * @author system
 * @since 2026-03-11
 */
@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {
}
