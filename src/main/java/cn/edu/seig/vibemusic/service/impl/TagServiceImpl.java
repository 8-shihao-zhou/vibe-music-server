package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.mapper.PostTagMapper;
import cn.edu.seig.vibemusic.mapper.TagStatsMapper;
import cn.edu.seig.vibemusic.model.entity.PostTag;
import cn.edu.seig.vibemusic.model.entity.TagStats;
import cn.edu.seig.vibemusic.model.vo.TagVO;
import cn.edu.seig.vibemusic.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 *
 * @author system
 * @since 2026-03-11
 */
@Slf4j
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagStatsMapper tagStatsMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<TagVO> getHotTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        // 查询使用次数最多的标签
        LambdaQueryWrapper<TagStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TagStats::getUseCount)
                .last("LIMIT " + limit);

        List<TagStats> tagStatsList = tagStatsMapper.selectList(wrapper);

        return tagStatsList.stream().map(stats -> {
            TagVO vo = new TagVO();
            BeanUtils.copyProperties(stats, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<TagVO> searchTags(String keyword, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }

        Page<TagStats> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TagStats> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(TagStats::getTagName, keyword.trim());
        }

        wrapper.orderByDesc(TagStats::getUseCount);

        Page<TagStats> tagStatsPage = tagStatsMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<TagVO> voPage = new Page<>(tagStatsPage.getCurrent(), tagStatsPage.getSize(), tagStatsPage.getTotal());
        List<TagVO> voList = tagStatsPage.getRecords().stream().map(stats -> {
            TagVO vo = new TagVO();
            BeanUtils.copyProperties(stats, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePostTags(Long postId, String tags) {
        if (postId == null || tags == null || tags.trim().isEmpty()) {
            return;
        }

        try {
            // 解析标签JSON数组
            List<String> tagList = objectMapper.readValue(tags, new TypeReference<List<String>>() {});
            if (tagList == null || tagList.isEmpty()) {
                return;
            }

            // 删除旧的标签关联
            deletePostTags(postId);

            // 插入新的标签关联
            for (String tagName : tagList) {
                if (tagName == null || tagName.trim().isEmpty()) {
                    continue;
                }

                tagName = tagName.trim();

                // 插入帖子标签关联
                PostTag postTag = new PostTag();
                postTag.setPostId(postId);
                postTag.setTagName(tagName);
                postTagMapper.insert(postTag);

                // 更新标签统计
                updateTagStats(tagName);
            }

        } catch (Exception e) {
            log.error("更新帖子标签失败: postId={}, tags={}", postId, tags, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePostTags(Long postId) {
        if (postId == null) {
            return;
        }

        // 查询该帖子的所有标签
        LambdaQueryWrapper<PostTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostTag::getPostId, postId);
        List<PostTag> postTags = postTagMapper.selectList(wrapper);

        if (postTags != null && !postTags.isEmpty()) {
            // 删除标签关联
            postTagMapper.delete(wrapper);

            // 更新标签统计（减少计数）
            for (PostTag postTag : postTags) {
                decreaseTagStats(postTag.getTagName());
            }
        }
    }

    /**
     * 更新标签统计（增加计数）
     */
    private void updateTagStats(String tagName) {
        LambdaQueryWrapper<TagStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagStats::getTagName, tagName);
        TagStats stats = tagStatsMapper.selectOne(wrapper);

        if (stats == null) {
            // 创建新标签统计
            stats = new TagStats();
            stats.setTagName(tagName);
            stats.setUseCount(1);
            stats.setPostCount(1);
            stats.setLastUsedTime(LocalDateTime.now());
            tagStatsMapper.insert(stats);
        } else {
            // 更新现有标签统计
            stats.setUseCount(stats.getUseCount() + 1);
            stats.setPostCount(stats.getPostCount() + 1);
            stats.setLastUsedTime(LocalDateTime.now());
            tagStatsMapper.updateById(stats);
        }
    }

    /**
     * 更新标签统计（减少计数）
     */
    private void decreaseTagStats(String tagName) {
        LambdaQueryWrapper<TagStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagStats::getTagName, tagName);
        TagStats stats = tagStatsMapper.selectOne(wrapper);

        if (stats != null) {
            stats.setUseCount(Math.max(0, stats.getUseCount() - 1));
            stats.setPostCount(Math.max(0, stats.getPostCount() - 1));
            tagStatsMapper.updateById(stats);

            // 如果使用次数为0，可以选择删除该标签统计
            if (stats.getUseCount() == 0) {
                tagStatsMapper.deleteById(stats.getId());
            }
        }
    }
}
