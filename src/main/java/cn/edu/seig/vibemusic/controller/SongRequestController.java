package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.model.dto.SongRequestDTO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.ISongRequestService;
import cn.edu.seig.vibemusic.service.MinioService;
import cn.edu.seig.vibemusic.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/song-request")
public class SongRequestController {

    @Autowired
    private ISongRequestService songRequestService;

    @Autowired
    private MinioService minioService;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /** 上传封面或音频文件，返回代理 URL（走后端转发，无跨域问题） */
    @PostMapping("/upload")
    public Result uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "type", defaultValue = "covers") String type) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        String folder = type.equals("audios") ? "song-request/audios" : "song-request/covers";
        String minioUrl = minioService.uploadFile(file, folder);
        System.out.println("[SongRequest] 文件上传成功，MinIO URL: " + minioUrl);
        // 返回后端代理 URL，前端通过后端访问，避免跨域
        String proxyUrl = "http://localhost:8080/file/proxy?path=" + java.net.URLEncoder.encode(minioUrl, java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("[SongRequest] 代理 URL: " + proxyUrl);
        return Result.success("上传成功", proxyUrl);
    }

    /** 提交收录请求 */
    @PostMapping("/submit")
    public Result submit(@RequestBody @Valid SongRequestDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        return songRequestService.submitRequest(dto, userId);
    }

    /** 查看我的请求列表 */
    @GetMapping("/my")
    public Result myRequests(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) return Result.error("请先登录");
        return songRequestService.getUserRequests(userId, pageNum, pageSize);
    }

    // ==================== 管理端接口 ====================

    /** 分页查询所有请求 */
    @GetMapping("/admin/list")
    public Result adminList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return songRequestService.adminListRequests(status, keyword, pageNum, pageSize);
    }

    /** 通过请求 */
    @PutMapping("/admin/approve/{id}")
    public Result approve(@PathVariable Long id) {
        return songRequestService.approveRequest(id);
    }

    /** 拒绝请求 */
    @PutMapping("/admin/reject/{id}")
    public Result reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        return songRequestService.rejectRequest(id, reason);
    }

    /** 批量删除请求记录 */
    @DeleteMapping("/admin/delete")
    public Result delete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Result.error("请选择要删除的记录");
        return songRequestService.deleteRequests(ids);
    }
}
