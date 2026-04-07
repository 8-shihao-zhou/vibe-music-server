package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.service.AIService;
import cn.edu.seig.vibemusic.utils.UserContext;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * AI MV 生成服务实现
 */
@Service
public class AIServiceImpl implements AIService {

    @Value("${ai.server-url}")
    private String aiServerUrl;

    @Value("${ai.storage-path}")
    private String storagePath;

    /**
     * 获取当前登录用户的专属存储目录
     */
    private String getCurrentUserStoragePath() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return getUserStoragePath(userId);
    }

    /**
     * 根据用户 ID 获取专属存储目录
     */
    private String getUserStoragePath(Long userId) {
        String userPath = storagePath + "user_" + userId + File.separator;
        if (!FileUtil.exist(userPath)) {
            FileUtil.mkdir(userPath);
        }
        return userPath;
    }

    /**
     * 兼容旧接口，支持通过上传文件同步生成
     */
    @Override
    public String generateVideo(MultipartFile file, String songName) {
        String userStoragePath = getCurrentUserStoragePath();
        Long userId = UserContext.getUserId();
        String tempMp3Name = IdUtil.simpleUUID() + ".mp3";
        File tempMp3File = new File(userStoragePath + tempMp3Name);

        try {
            file.transferTo(tempMp3File);
            return requestAiAndSaveVideo(tempMp3File, userStoragePath, userId, songName, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("视频生成失败: " + e.getMessage());
        } finally {
            FileUtil.del(tempMp3File);
        }
    }

    /**
     * 根据曲库音频地址生成 MV
     */
    @Override
    public String generateVideoFromAudioUrl(Long userId, String audioUrl, String songName, String styleCode) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        if (audioUrl == null || audioUrl.trim().isEmpty()) {
            throw new RuntimeException("音频地址不能为空");
        }

        String userStoragePath = getUserStoragePath(userId);
        String tempMp3Name = IdUtil.simpleUUID() + ".mp3";
        File tempMp3File = new File(userStoragePath + tempMp3Name);

        try (HttpResponse response = HttpRequest.get(audioUrl).timeout(-1).execute()) {
            if (!response.isOk()) {
                throw new RuntimeException("下载曲库音频失败，状态码: " + response.getStatus());
            }

            FileUtil.writeBytes(response.bodyBytes(), tempMp3File);
            return requestAiAndSaveVideo(tempMp3File, userStoragePath, userId, songName, styleCode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("根据曲库音频生成 MV 失败: " + e.getMessage());
        } finally {
            FileUtil.del(tempMp3File);
        }
    }

    /**
     * 统一调用学校服务器并保存生成结果
     */
    private String requestAiAndSaveVideo(File tempMp3File, String userStoragePath, Long userId, String songName, String styleCode) {
        System.out.println(">>> [Java] MP3 已准备完成，正在请求 AI 服务: " + aiServerUrl);

        HttpRequest request = HttpRequest.post(aiServerUrl)
                .form("file", tempMp3File)
                .timeout(-1);

        // 透传风格参数，供学校服务器上的 music2video 使用
        if (styleCode != null && !styleCode.trim().isEmpty()) {
            request.form("style", styleCode.trim());
        }

        try (HttpResponse response = request.execute()) {

            if (!response.isOk()) {
                throw new RuntimeException("AI 服务内部错误，状态码: " + response.getStatus());
            }

            byte[] videoBytes = response.bodyBytes();
            String videoName = "mv_" + IdUtil.simpleUUID() + ".mp4";
            String finalVideoPath = userStoragePath + videoName;
            FileUtil.writeBytes(videoBytes, finalVideoPath);
            System.out.println(">>> [Java] 视频生成成功，已保存: " + finalVideoPath);

            if (songName != null && !songName.trim().isEmpty()) {
                saveSongNameMapping(userStoragePath, videoName, songName.trim());
            }

            return "http://localhost:8080/files/user_" + userId + "/" + videoName;
        }
    }

    /**
     * 保存文件名和歌曲名的映射关系
     */
    private void saveSongNameMapping(String userStoragePath, String fileName, String songName) {
        try {
            String mappingFilePath = userStoragePath + "song_mapping.json";
            File mappingFile = new File(mappingFilePath);

            JSONObject mappings;
            if (mappingFile.exists()) {
                String content = FileUtil.readString(mappingFile, StandardCharsets.UTF_8);
                mappings = JSONUtil.parseObj(content);
            } else {
                mappings = new JSONObject();
            }

            mappings.set(fileName, songName);
            FileUtil.writeString(mappings.toString(), mappingFile, StandardCharsets.UTF_8);
            System.out.println(">>> [Java] 已保存歌曲名映射: " + fileName + " -> " + songName);
        } catch (Exception e) {
            System.err.println(">>> [Java] 保存歌曲名映射失败: " + e.getMessage());
        }
    }
}
