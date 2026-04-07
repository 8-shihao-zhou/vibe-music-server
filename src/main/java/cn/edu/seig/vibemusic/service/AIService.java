package cn.edu.seig.vibemusic.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * AI视频生成服务接口
 */
public interface AIService {

    /**
     * 调用 AI 生成视频
     * @param file 前端上传的音频文件
     * @param songName 歌曲名称（可选）
     * @return 生成的视频访问 URL
     */
    String generateVideo(MultipartFile file, String songName);

    /**
     * 根据曲库音频地址生成 MV
     */
    String generateVideoFromAudioUrl(Long userId, String audioUrl, String songName, String styleCode);
}
