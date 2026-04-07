package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.enums.AgentActionType;
import cn.edu.seig.vibemusic.agent.model.vo.AgentActionVO;
import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import cn.edu.seig.vibemusic.mapper.SongMapper;
import cn.edu.seig.vibemusic.model.vo.SongVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 音乐相关工具。
 *
 * 这个工具既提供给 LangChain4j 的大模型调用，
 * 也保留结构化方法，方便后端内部复用和排查。
 */
@Component
@RequiredArgsConstructor
public class AgentMusicTool {

    private final SongMapper songMapper;
    private final AgentRuntimeContext agentRuntimeContext;

    /**
     * 让大模型根据用户原话搜索歌曲，并在命中时准备播放动作。
     *
     * @param keyword 用户原话或歌曲关键词
     * @return 便于模型理解的文本结果
     */
    @Tool("根据用户原话搜索最匹配的歌曲，并在命中时准备播放动作。适用于播放歌曲、来一首、听歌等请求。")
    public String searchSong(String keyword) {
        AgentToolDataVO data = searchSongData(keyword);
        agentRuntimeContext.setToolData(data);

        if (!Boolean.TRUE.equals(data.getSuccess())) {
            return "未找到匹配歌曲";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("songId", data.getSongId());
        payload.put("songName", data.getSongName());
        payload.put("artistName", data.getArtistName());
        payload.put("coverUrl", data.getCoverUrl());
        payload.put("audioUrl", data.getAudioUrl());

        agentRuntimeContext.addAction(new AgentActionVO(AgentActionType.PLAY_SONG.getCode(), payload));

        return String.format(
                "已找到歌曲：%s，songId=%d，artist=%s，album=%s，已准备播放动作",
                data.getSongName(),
                data.getSongId(),
                data.getArtistName(),
                data.getAlbum()
        );
    }

    /**
     * 后端结构化查歌方法。
     *
     * @param keyword 用户原始输入
     * @return 结构化歌曲结果
     */
    public AgentToolDataVO searchSongData(String keyword) {
        AgentToolDataVO data = new AgentToolDataVO();
        data.setSuccess(false);

        if (keyword == null || keyword.trim().isEmpty()) {
            data.setMessage("歌曲关键词不能为空");
            return data;
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        String artistName = null;
        String songName = normalizedKeyword;

        // 支持“周杰伦的晴天”这种说法
        if (normalizedKeyword.contains("的")) {
            String[] parts = normalizedKeyword.split("的", 2);
            if (parts.length == 2) {
                artistName = safeTrim(parts[0]);
                songName = safeTrim(parts[1]);
            }
        }

        SongVO matchedSong = queryFirstSong(songName, artistName);

        // 没查到时，退化成只按原始关键词查歌曲名
        if (matchedSong == null) {
            matchedSong = queryFirstSong(normalizedKeyword, null);
        }

        // 再退一步，尝试仅按歌手名兜底
        if (matchedSong == null && artistName != null) {
            matchedSong = queryFirstSong(null, artistName);
        }

        if (matchedSong == null) {
            data.setMessage("未找到匹配歌曲");
            return data;
        }

        data.setSuccess(true);
        data.setMessage("查歌成功");
        data.setSongId(matchedSong.getSongId());
        data.setSongName(matchedSong.getSongName());
        data.setArtistName(matchedSong.getArtistName());
        data.setAlbum(matchedSong.getAlbum());
        data.setCoverUrl(matchedSong.getCoverUrl());
        data.setAudioUrl(matchedSong.getAudioUrl());
        return data;
    }

    /**
     * 查询第一页最匹配的歌曲
     */
    private SongVO queryFirstSong(String songName, String artistName) {
        Page<SongVO> page = new Page<>(1, 5);
        IPage<SongVO> resultPage = songMapper.getSongsWithArtist(page, songName, artistName, null);
        List<SongVO> records = resultPage.getRecords();
        if (records == null || records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    /**
     * 清洗用户输入，尽量提取真正的歌名关键词
     */
    private String normalizeKeyword(String keyword) {
        return keyword
                .replace("播放歌曲", "")
                .replace("播放", "")
                .replace("来一首", "")
                .replace("来首", "")
                .replace("给我放", "")
                .replace("帮我放", "")
                .replace("我想听", "")
                .replace("听一下", "")
                .trim();
    }

    /**
     * 安全去空格，避免空指针
     */
    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }
}
