package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.service.AIService;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class AIServiceImpl implements AIService {

    @Value("${ai.server-url}")
    private String aiServerUrl; // http://localhost:8000/generate

    @Value("${ai.storage-path}")
    private String storagePath; // D:/music-video-storage/

    /**
     * 调用 AI 生成视频
     * @param file 前端上传的音频文件
     * @param songName 歌曲名称（可选）
     * @return 生成的视频访问 URL
     */
    @Override
    public String generateVideo(MultipartFile file, String songName) {
        // 1. 确保本地存储目录存在
        if (!FileUtil.exist(storagePath)) {
            FileUtil.mkdir(storagePath);
        }

        // 2. 将前端上传的 MP3 暂存为文件 (因为 Hutool 发请求需要 File 对象)
        String tempMp3Name = IdUtil.simpleUUID() + ".mp3";
        File tempMp3File = new File(storagePath + tempMp3Name);

        try {
            file.transferTo(tempMp3File);
            System.out.println(">>> [Java] MP3已暂存，正在请求 AI 服务: " + aiServerUrl);

            // 3. 发送 POST 请求给学校服务器 (Python)
            // timeout 设置为 10分钟 (600000ms)，防止生成时间过长导致断开
            try (HttpResponse response = HttpRequest.post(aiServerUrl)
                    .form("file", tempMp3File) // 参数名 'file' 对应 Python 端
                    .timeout(-1)
                    .execute()) {

                // 4. 处理 Python 返回的结果
                if (response.isOk()) {
                    // 获取视频二进制流
                    byte[] videoBytes = response.bodyBytes();

                    // 生成视频文件名
                    String videoName = "mv_" + IdUtil.simpleUUID() + ".mp4";
                    String finalVideoPath = storagePath + videoName;

                    // 写入硬盘
                    FileUtil.writeBytes(videoBytes, finalVideoPath);
                    System.out.println(">>> [Java] 视频生成成功！已保存: " + finalVideoPath);

                    // 5. 保存歌曲名映射关系
                    if (songName != null && !songName.isEmpty()) {
                        saveSongNameMapping(videoName, songName);
                    }

                    // 6. 拼接成前端可访问的 URL
                    // 假设你的后端端口是 8080，路径映射是 /files/
                    // 返回: http://localhost:8080/files/mv_xxxx.mp4
                    return "http://localhost:8080/files/" + videoName;
                } else {
                    throw new RuntimeException("AI 服务内部错误，状态码: " + response.getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 抛出异常，让 Controller 捕获并返回 Result.error
            throw new RuntimeException("视频生成失败: " + e.getMessage());
        } finally {
            // 7. 无论成功失败，都删掉那个临时的 MP3 文件，节省空间
            FileUtil.del(tempMp3File);
        }
    }

    /**
     * 保存文件名和歌曲名的映射关系到 JSON 文件
     */
    private void saveSongNameMapping(String fileName, String songName) {
        try {
            String mappingFilePath = storagePath + "song_mapping.json";
            File mappingFile = new File(mappingFilePath);
            
            JSONObject mappings;
            if (mappingFile.exists()) {
                // 读取现有映射
                String content = FileUtil.readString(mappingFile, StandardCharsets.UTF_8);
                mappings = JSONUtil.parseObj(content);
            } else {
                // 创建新映射
                mappings = new JSONObject();
            }
            
            // 添加新映射
            mappings.set(fileName, songName);
            
            // 写回文件
            FileUtil.writeString(mappings.toString(), mappingFile, StandardCharsets.UTF_8);
            System.out.println(">>> [Java] 已保存歌曲名映射: " + fileName + " -> " + songName);
        } catch (Exception e) {
            System.err.println(">>> [Java] 保存歌曲名映射失败: " + e.getMessage());
        }
    }
}