package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.model.dto.AiVideoTaskCreateDTO;
import cn.edu.seig.vibemusic.model.vo.AiVideoTaskVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.AIService;
import cn.edu.seig.vibemusic.service.IAiVideoTaskService;
import cn.edu.seig.vibemusic.service.IPointsService;
import cn.edu.seig.vibemusic.utils.UserContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private IPointsService pointsService;

    @Autowired
    private IAiVideoTaskService aiVideoTaskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.storage-path}")
    private String storagePath;

    private static final String FILE_URL_PREFIX = "http://localhost:8080/files/";

    /**
     * 兼容旧版同步生成接口
     */
    @PostMapping("/generate")
    public Result<String> generateVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "songName", required = false) String songName) {
        if (file.isEmpty()) {
            return Result.error("请先上传音频文件");
        }

        try {
            String videoUrl = aiService.generateVideo(file, songName);

            Long userId = UserContext.getUserId();
            if (userId != null) {
                try {
                    pointsService.addPoints(userId, "MV_CREATE", null);
                } catch (Exception e) {
                    System.err.println("AI 创作积分增加失败: " + e.getMessage());
                }
            }

            return Result.success(videoUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建异步 AI MV 任务
     */
    @PostMapping("/tasks")
    public Result<AiVideoTaskVO> createTask(@RequestBody AiVideoTaskCreateDTO dto) {
        Long userId = UserContext.getUserId();
        return aiVideoTaskService.createTask(userId, dto);
    }

    /**
     * 获取当前用户的任务列表
     */
    @GetMapping("/tasks")
    public Result<List<AiVideoTaskVO>> getTaskList() {
        Long userId = UserContext.getUserId();
        return aiVideoTaskService.getCurrentUserTasks(userId);
    }

    /**
     * 删除当前用户的任务记录
     */
    @DeleteMapping("/tasks/{taskId}")
    public Result<String> deleteTask(@PathVariable("taskId") Long taskId) {
        Long userId = UserContext.getUserId();
        return aiVideoTaskService.deleteCurrentUserTask(userId, taskId);
    }

    /**
     * 获取当前用户的历史生成记录
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getHistory() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        String userStoragePath = storagePath + "user_" + userId + File.separator;
        File dir = new File(userStoragePath);
        if (!dir.exists()) {
            return Result.success(new ArrayList<>());
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return Result.success(new ArrayList<>());
        }

        Map<String, String> songMappings = readSongMappings(userStoragePath);
        final Map<String, String> finalMappings = songMappings;

        List<Map<String, Object>> list = Arrays.stream(files)
                .filter(file -> file.getName().toLowerCase().endsWith(".mp4"))
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(file -> {
                    Map<String, Object> map = new HashMap<>();
                    String fileName = file.getName();
                    String mvName = stripMp4Suffix(fileName);

                    map.put("fileName", fileName);
                    map.put("mvName", mvName);
                    map.put("songName", finalMappings.getOrDefault(fileName, ""));
                    map.put("url", buildFileUrl(userId, fileName));
                    map.put("createTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new Date(file.lastModified())));
                    map.put("size", String.format("%.2f MB", file.length() / 1024.0 / 1024.0));
                    return map;
                })
                .collect(Collectors.toList());

        return Result.success(list);
    }

    /**
     * 重命名作品文件，并同步更新歌曲映射与任务记录
     */
    @PutMapping("/rename")
    public Result<String> renameMvFile(
            @RequestParam("oldFileName") String oldFileName,
            @RequestParam("newFileName") String newFileName) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        if (oldFileName == null || oldFileName.trim().isEmpty()) {
            return Result.error("原文件名不能为空");
        }
        if (newFileName == null || newFileName.trim().isEmpty()) {
            return Result.error("新文件名不能为空");
        }

        String normalizedName = newFileName.trim();
        if (normalizedName.toLowerCase().endsWith(".mp4")) {
            normalizedName = normalizedName.substring(0, normalizedName.length() - 4);
        }

        if (normalizedName.matches(".*[/\\\\:*?\"<>|].*")) {
            return Result.error("文件名不能包含特殊字符 / \\ : * ? \" < > |");
        }

        try {
            String userStoragePath = storagePath + "user_" + userId + File.separator;
            File dir = new File(userStoragePath);
            if (!dir.exists()) {
                return Result.error("用户目录不存在");
            }

            File oldFile = new File(dir, oldFileName);
            if (!oldFile.exists()) {
                return Result.error("原文件不存在");
            }

            String newFileNameWithExt = normalizedName + ".mp4";
            File newFile = new File(dir, newFileNameWithExt);
            if (newFile.exists() && !oldFile.equals(newFile)) {
                return Result.error("文件名已存在");
            }

            boolean success = oldFile.renameTo(newFile);
            if (!success) {
                return Result.error("重命名失败");
            }

            syncSongMappingsAfterRename(userStoragePath, oldFileName, newFileNameWithExt);
            aiVideoTaskService.syncTaskMediaAfterRename(userId, oldFileName, newFileNameWithExt);
            return Result.success("重命名成功");
        } catch (Exception e) {
            return Result.error("重命名失败：" + e.getMessage());
        }
    }

    /**
     * 读取歌曲名映射
     */
    private Map<String, String> readSongMappings(String userStoragePath) {
        File mappingFile = new File(userStoragePath + "song_mapping.json");
        if (!mappingFile.exists()) {
            return new HashMap<>();
        }

        try {
            String content = Files.readString(mappingFile.toPath(), StandardCharsets.UTF_8);
            return objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            System.err.println("读取歌曲映射失败: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 在重命名作品后，同步更新 song_mapping.json 中的键名。
     */
    private void syncSongMappingsAfterRename(String userStoragePath, String oldFileName, String newFileName) {
        File mappingFile = new File(userStoragePath + "song_mapping.json");
        if (!mappingFile.exists()) {
            return;
        }

        try {
            Map<String, String> mappings = readSongMappings(userStoragePath);
            if (!mappings.containsKey(oldFileName)) {
                return;
            }

            String songName = mappings.remove(oldFileName);
            Map<String, String> orderedMappings = new LinkedHashMap<>(mappings);
            orderedMappings.put(newFileName, songName);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(mappingFile, orderedMappings);
        } catch (Exception e) {
            System.err.println("同步歌曲映射失败: " + e.getMessage());
        }
    }

    private String buildFileUrl(Long userId, String fileName) {
        return FILE_URL_PREFIX + "user_" + userId + "/" + fileName;
    }

    /**
     * 去掉 mp4 后缀，便于前端直接展示作品名
     */
    private String stripMp4Suffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        if (fileName.toLowerCase().endsWith(".mp4")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }
}
