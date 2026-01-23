package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 👈 必须导入这个
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    // 👇👇👇 【修复点】在这里注入存储路径 👇👇👇
    @Value("${ai.storage-path}")
    private String storagePath;
    // 👆👆👆 加上这一行，storagePath 就不报错了

    /**
     * 生成 AI 音乐视频
     * 地址: POST http://localhost:8080/api/ai/generate
     */
    @PostMapping("/generate")
    public Result<String> generateVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "songName", required = false) String songName) {
        // 1. 校验文件
        if (file.isEmpty()) {
            return Result.error("请上传音频文件");
        }

        try {
            // 2. 调用 Service 核心逻辑，传递歌曲名
            String videoUrl = aiService.generateVideo(file, songName);

            // 3. 返回成功结果 (code=0, data=视频地址)
            return Result.success(videoUrl);

        } catch (Exception e) {
            // 4. 返回失败结果 (code=1, msg=错误信息)
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取历史生成记录列表
     * 地址: GET http://localhost:8080/api/ai/history
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory() {
        // 现在这里可以识别 storagePath 了
        File dir = new File(storagePath);

        if (!dir.exists()) {
            return Result.success(new ArrayList<>());
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return Result.success(new ArrayList<>());
        }

        // 读取歌曲名映射文件
        Map<String, String> songMappings = new HashMap<>();
        File mappingFile = new File(storagePath + "song_mapping.json");
        if (mappingFile.exists()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(mappingFile.toPath()));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                songMappings = mapper.readValue(content, Map.class);
            } catch (Exception e) {
                System.err.println("读取歌曲名映射文件失败: " + e.getMessage());
            }
        }

        // 1. 过滤出 .mp4 文件
        // 2. 按最后修改时间倒序排列 (最新的在前面)
        // 3. 封装成前端需要的格式
        final Map<String, String> finalMappings = songMappings;
        List<Map<String, Object>> list = Arrays.stream(files)
                .filter(f -> f.getName().toLowerCase().endsWith(".mp4"))
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(f -> {
                    Map<String, Object> map = new HashMap<>();
                    String fileName = f.getName();
                    // 优先使用歌曲名，如果没有则使用文件名
                    String displayName = finalMappings.getOrDefault(fileName, fileName);
                    map.put("fileName", displayName);
                    // 拼接完整访问 URL
                    map.put("url", "http://localhost:8080/files/" + fileName);
                    // 格式化时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("createTime", sdf.format(new Date(f.lastModified())));
                    // 文件大小 (MB)
                    map.put("size", String.format("%.2f MB", f.length() / 1024.0 / 1024.0));
                    return map;
                })
                .collect(Collectors.toList());

        return Result.success(list);
    }
}