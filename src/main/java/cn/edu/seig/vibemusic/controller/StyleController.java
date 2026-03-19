package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.mapper.SongMapper;
import cn.edu.seig.vibemusic.mapper.StyleMapper;
import cn.edu.seig.vibemusic.model.entity.Style;
import cn.edu.seig.vibemusic.model.vo.SongVO;
import cn.edu.seig.vibemusic.result.PageResult;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/style")
public class StyleController {

    @Autowired
    private StyleMapper styleMapper;

    @Autowired
    private SongMapper songMapper;

    /** 获取所有风格及各风格歌曲数量 */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listStyles() {
        List<Style> styles = styleMapper.selectList(new QueryWrapper<Style>().orderByAsc("id"));
        List<Map<String, Object>> result = styles.stream().map(s -> {
            // 查该风格下的歌曲数
            long count = songMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<cn.edu.seig.vibemusic.model.entity.Song>()
                    .inSql("id", "SELECT song_id FROM tb_genre WHERE style_id = " + s.getStyleId())
            );
            return Map.<String, Object>of(
                "styleId", s.getStyleId(),
                "name", s.getName(),
                "songCount", count
            );
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /** 按风格分页查询歌曲 */
    @GetMapping("/songs")
    public Result<PageResult<SongVO>> getSongsByStyle(
            @RequestParam Long styleId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        if (styleId == null || styleId <= 0) {
            return Result.error("无效的风格ID");
        }
        Page<SongVO> page = new Page<>(pageNum, pageSize);
        IPage<SongVO> result = songMapper.getSongsByStyleId(page, styleId);
        return Result.success(new PageResult<>(result.getTotal(), result.getRecords()));
    }
}
