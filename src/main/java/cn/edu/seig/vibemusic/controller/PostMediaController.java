package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IUserMvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 帖子媒体控制器
 *
 * @author system
 * @since 2026-03-16
 */
@RestController
@RequestMapping("/community/post")
public class PostMediaController {

    @Autowired
    private IUserMvService userMvService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:9000}")
    private String urlPrefix;

    /**
     * 上传图片
     *
     * @param files 图片文件（支持多图）
     * @return 图片URL列表
     */
    @PostMapping("/upload/images")
    public Result uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的图片");
        }

        if (files.length > 9) {
            return Result.error("最多只能上传9张图片");
        }

        List<String> imageUrls = new ArrayList<>();

        try {
            // 创建上传目录
            String datePath = java.time.LocalDate.now().toString().replace("-", "/");
            String uploadDir = uploadPath + "/post-images/" + datePath;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 处理每个文件
            for (MultipartFile file : files) {
                // 验证文件类型
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return Result.error("只能上传图片文件");
                }

                // 验证文件大小（最大5MB）
                if (file.getSize() > 5 * 1024 * 1024) {
                    return Result.error("图片大小不能超过5MB");
                }

                // 生成唯一文件名
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + extension;

                // 保存文件
                Path filePath = Paths.get(uploadDir, filename);
                Files.copy(file.getInputStream(), filePath);

                // 生成访问URL
                String imageUrl = urlPrefix + "/uploads/post-images/" + datePath + "/" + filename;
                imageUrls.add(imageUrl);
            }

            return Result.success(imageUrls);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的MV列表
     *
     * @param userId 用户ID（可选，默认当前用户）
     * @param status 状态（可选，1-已完成）
     * @return MV列表
     */
    @GetMapping("/user-mvs")
    public Result getUserMvList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "1") Integer status) {
        return userMvService.getUserMvList(userId, status);
    }

    /**
     * 获取MV详情
     *
     * @param mvId MV ID
     * @return MV详情
     */
    @GetMapping("/mv/{id}")
    public Result getMvDetail(@PathVariable("id") Long mvId) {
        return userMvService.getMvDetail(mvId);
    }

    /**
     * 同步本地MV文件到数据库
     *
     * @param userId 用户ID（可选，默认当前用户）
     * @return 同步结果
     */
    @PostMapping("/sync-mvs")
    public Result syncMvFiles(@RequestParam(required = false) Long userId) {
        return userMvService.syncMvFiles(userId);
    }
}
