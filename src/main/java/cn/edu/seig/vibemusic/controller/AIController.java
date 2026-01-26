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
     * 获取历史生成记录列表（仅返回当前用户的）
     * 地址: GET http://localhost:8080/api/ai/history
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory() {
        // 获取当前登录用户ID
        Long userId = cn.edu.seig.vibemusic.utils.UserContext.getUserId();
        System.out.println(">>> [DEBUG] 当前用户ID: " + userId);
        
        if (userId == null) {
            System.out.println(">>> [DEBUG] 用户未登录");
            return Result.error("用户未登录");
        }

        // 用户专属目录
        String userStoragePath = storagePath + "user_" + userId + File.separator;
        System.out.println(">>> [DEBUG] 用户存储路径: " + userStoragePath);
        
        File dir = new File(userStoragePath);
        System.out.println(">>> [DEBUG] 目录是否存在: " + dir.exists());

        if (!dir.exists()) {
            System.out.println(">>> [DEBUG] 目录不存在，返回空列表");
            return Result.success(new ArrayList<>());
        }

        File[] files = dir.listFiles();
        System.out.println(">>> [DEBUG] 文件数量: " + (files != null ? files.length : 0));
        
        if (files == null) {
            System.out.println(">>> [DEBUG] 无法读取文件列表");
            return Result.success(new ArrayList<>());
        }

<<<<<<< HEAD
        // 打印所有文件名
        for (File f : files) {
            System.out.println(">>> [DEBUG] 发现文件: " + f.getName() + " (是否为mp4: " + f.getName().toLowerCase().endsWith(".mp4") + ")");
        }

        // 读取歌曲名映射文件
        Map<String, String> songMappings = new HashMap<>();
        File mappingFile = new File(userStoragePath + "song_mapping.json");
=======
        // 读取歌曲名映射文件
        Map<String, String> songMappings = new HashMap<>();
        File mappingFile = new File(storagePath + "song_mapping.json");
>>>>>>> 8da8e4f07010a281660b293a79da2621f643f564
        if (mappingFile.exists()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(mappingFile.toPath()));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                songMappings = mapper.readValue(content, Map.class);
<<<<<<< HEAD
                System.out.println(">>> [DEBUG] 读取到歌曲映射: " + songMappings.size() + " 条");
            } catch (Exception e) {
                System.err.println(">>> [DEBUG] 读取歌曲名映射文件失败: " + e.getMessage());
=======
            } catch (Exception e) {
                System.err.println("读取歌曲名映射文件失败: " + e.getMessage());
>>>>>>> 8da8e4f07010a281660b293a79da2621f643f564
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
<<<<<<< HEAD
                    // 拼接完整访问 URL（包含用户ID路径）
                    map.put("url", "http://localhost:8080/files/user_" + userId + "/" + fileName);
=======
                    // 拼接完整访问 URL
                    map.put("url", "http://localhost:8080/files/" + fileName);
>>>>>>> 8da8e4f07010a281660b293a79da2621f643f564
                    // 格式化时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    map.put("createTime", sdf.format(new Date(f.lastModified())));
                    // 文件大小 (MB)
                    map.put("size", String.format("%.2f MB", f.length() / 1024.0 / 1024.0));
                    return map;
                })
                .collect(Collectors.toList());

        System.out.println(">>> [DEBUG] 返回MV数量: " + list.size());
        return Result.success(list);
    }
}