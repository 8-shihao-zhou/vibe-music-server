package cn.edu.seig.vibemusic.agent.tool;

import cn.edu.seig.vibemusic.agent.model.vo.AgentToolDataVO;
import cn.edu.seig.vibemusic.mapper.SongMapper;
import cn.edu.seig.vibemusic.model.vo.SongVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 音乐相关工具
 */
@Component
@RequiredArgsConstructor
public class AgentMusicTool {

    private final SongMapper songMapper;

    /**
     * 提供给 LangChain4j 的工具方法
     * 返回文本，方便模型理解
     */
    @Tool("根据歌曲关键词搜索最匹配的歌曲，返回歌曲信息")
    public String searchSong(String keyword) {
        AgentToolDataVO data = searchSongData(keyword);
        if (Boolean.TRUE.equals(data.getSuccess())) {
            return String.format(
                    "找到歌曲：%s，songId=%d，artist=%s，album=%s",
                    data.getSongName(),
                    data.getSongId(),
                    data.getArtistName(),
                    data.getAlbum()
            );
        }
        return "未找到匹配歌曲";
    }

    /**
     * 后端真正使用的结构化查询方法
     * 这里不直接给模型用，而是给 Service 组装动作协议
     */
    public AgentToolDataVO searchSongData(String keyword) {
        AgentToolDataVO data = new AgentToolDataVO();
        data.setSuccess(false);

        if (keyword == null || keyword.trim().isEmpty()) {
            data.setMessage("歌曲关键词不能为空");
            return data;
        }

        String normalizedKeyword = normalizeKeyword(keyword);

        // 先按“歌手的歌曲名”格式拆分
        String artistName = null;
        String songName = normalizedKeyword;

        if (normalizedKeyword.contains("的")) {
            String[] parts = normalizedKeyword.split("的", 2);
            if (parts.length == 2) {
                artistName = safeTrim(parts[0]);
                songName = safeTrim(parts[1]);
            }
        }

        // 第一轮：按歌曲名 + 歌手名联合查
        SongVO matchedSong = queryFirstSong(songName, artistName);

        // 第二轮：只按歌曲名查
        if (matchedSong == null) {
            matchedSong = queryFirstSong(normalizedKeyword, null);
        }

        // 第三轮：如果像“播放周杰伦”这种，再尝试按歌手名查
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
     * 查询第一页最匹配歌曲
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
     * 清洗用户原始输入
     */
    private String normalizeKeyword(String keyword) {
        return keyword
                .replace("播放", "")
                .replace("来一首", "")
                .replace("来首", "")
                .replace("给我放", "")
                .replace("帮我放", "")
                .replace("我想听", "")
                .trim();
    }

    /**
     * 安全版去空格，防止空指针
     */
    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }
}
