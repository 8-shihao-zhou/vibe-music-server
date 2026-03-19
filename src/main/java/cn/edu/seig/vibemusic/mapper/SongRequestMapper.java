package cn.edu.seig.vibemusic.mapper;

import cn.edu.seig.vibemusic.model.entity.SongRequest;
import cn.edu.seig.vibemusic.model.vo.SongRequestVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SongRequestMapper extends BaseMapper<SongRequest> {

    Page<SongRequestVO> selectRequestPage(
            Page<SongRequestVO> page,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );

    Page<SongRequestVO> selectUserRequestPage(
            Page<SongRequestVO> page,
            @Param("userId") Long userId
    );
}
