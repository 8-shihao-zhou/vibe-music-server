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

    @Autowired
    private cn.edu.seig.vibemusic.service.IPointsService pointsService;

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

            // 3. AI创作成功后增加积分
            Long userId = cn.edu.seig.vibemusic.utils.UserContext.getUserId();
            if (userId != null) {
                try {
                    pointsService.addPoints(userId, "MV_CREATE", null);
                } catch (Exception e) {
                    System.err.println("增加AI创作积分失败: " + e.getMessage());
                }
            }

            // 4. 返回成功结果 (code=0, data=视频地址)
            return Result.success(videoUrl);

        } catch (Exception e) {
            // 5. 返回失败结果 (code=1, msg=错误信息)
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

        // 打印所有文件名
        for (File f : files) {
            System.out.println(">>> [DEBUG] 发现文件: " + f.getName() + " (是否为mp4: " + f.getName().toLowerCase().endsWith(".mp4") + ")");
        }

        // 读取歌曲名映射文件
        Map<String, String> songMappings = new HashMap<>();
        File mappingFile = new File(userStoragePath + "song_mapping.json");
        if (mappingFile.exists()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(mappingFile.toPath()));
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                songMappings = mapper.readValue(content, Map.class);
                System.out.println(">>> [DEBUG] 读取到歌曲映射: " + songMappings.size() + " 条");
            } catch (Exception e) {
                System.err.println(">>> [DEBUG] 读取歌曲名映射文件失败: " + e.getMessage());
            }
        }

        // 1. 过滤出 .mp4 文件（不限制文件名前缀）
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
                    // 拼接完整访问 URL（包含用户ID路径）
                    map.put("url", "http://localhost:8080/files/user_" + userId + "/" + fileName);
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

    /**
     * 重命名MV文件
     * 地址: PUT http://localhost:8080/api/ai/rename
     */
    @PutMapping("/rename")
    public Result<String> renameMvFile(
            @RequestParam("oldFileName") String oldFileName,
            @RequestParam("newFileName") String newFileName) {
        
        // 获取当前登录用户ID
        Long userId = cn.edu.seig.vibemusic.utils.UserContext.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        // 参数校验
        if (oldFileName == null || oldFileName.trim().isEmpty()) {
            return Result.error("原文件名不能为空");
        }
        if (newFileName == null || newFileName.trim().isEmpty()) {
            return Result.error("新文件名不能为空");
        }

        // 确保新文件名不包含.mp4扩展名（前端已处理，这里再次确认）
        newFileName = newFileName.trim();
        if (newFileName.toLowerCase().endsWith(".mp4")) {
            newFileName = newFileName.substring(0, newFileName.length() - 4);
        }

        // 验证文件名合法性（不能包含特殊字符）
        if (newFileName.matches(".*[/\\\\:*?\"<>|].*")) {
            return Result.error("文件名不能包含特殊字符: / \\ : * ? \" < > |");
        }

        try {
            // 用户专属目录
            String userStoragePath = storagePath + "user_" + userId + File.separator;
            File dir = new File(userStoragePath);
            
            if (!dir.exists()) {
                return Result.error("用户目录不存在");
            }

            // 旧文件
            File oldFile = new File(dir, oldFileName);
            if (!oldFile.exists()) {
                return Result.error("原文件不存在");
            }

            // 新文件名（保留.mp4扩展名）
            String newFileNameWithExt = newFileName + ".mp4";
            File newFile = new File(dir, newFileNameWithExt);
            
            // 检查新文件名是否已存在
            if (newFile.exists() && !oldFile.equals(newFile)) {
                return Result.error("文件名已存在");
            }

            // 重命名文件
            boolean success = oldFile.renameTo(newFile);
            if (!success) {
                return Result.error("重命名失败");
            }

            // 如果有对应的封面文件，也一起重命名
            String oldCoverName = oldFileName.replace("mv_", "cover_").replace(".mp4", ".jpg");
            File oldCoverFile = new File(dir, oldCoverName);
            if (oldCoverFile.exists()) {
                String newCoverName = newFileNameWithExt.replace(".mp4", ".jpg");
                File newCoverFile = new File(dir, newCoverName);
                oldCoverFile.renameTo(newCoverFile);
            }

            System.out.println(">>> [重命名] 成功: " + oldFileName + " -> " + newFileNameWithExt);
            return Result.success("重命名成功");

        } catch (Exception e) {
            System.err.println(">>> [重命名] 失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("重命名失败：" + e.getMessage());
        }
    }
}