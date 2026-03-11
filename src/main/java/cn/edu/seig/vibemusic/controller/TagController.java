package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.model.vo.TagVO;
import cn.edu.seig.vibemusic.service.TagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 *
 * @author system
 * @since 2026-03-11
 */
@Slf4j
@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public Result<List<TagVO>> getHotTags(
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        log.info(">>> [标签] 获取热门标签, limit={}", limit);
        List<TagVO> hotTags = tagService.getHotTags(limit);
        return Result.success(hotTags);
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public Result<Page<TagVO>> searchTags(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        log.info(">>> [标签] 搜索标签, keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        Page<TagVO> page = tagService.searchTags(keyword, pageNum, pageSize);
        return Result.success(page);
    }
}
