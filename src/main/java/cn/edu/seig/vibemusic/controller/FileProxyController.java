package cn.edu.seig.vibemusic.controller;

import cn.edu.seig.vibemusic.service.MinioService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

/**
 * 文件代理接口：将 MinIO 文件流转发给前端，解决跨域问题
 * GET /file/proxy?path=song-request/covers/xxx.png
 */
@RestController
@RequestMapping("/file")
public class FileProxyController {

    @Autowired
    private MinioService minioService;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @GetMapping("/proxy")
    public void proxy(@RequestParam("path") String path, HttpServletResponse response) {
        System.out.println("[FileProxy] 代理请求: " + path);
        try {
            // 如果传入的是完整 URL，提取 object path
            String objectPath = path;
            String prefix = endpoint + "/" + bucketName + "/";
            if (path.startsWith(prefix)) {
                objectPath = path.substring(prefix.length());
            } else if (path.startsWith("http://") || path.startsWith("https://")) {
                int idx = path.indexOf("/" + bucketName + "/");
                if (idx != -1) {
                    objectPath = path.substring(idx + bucketName.length() + 2);
                }
            }
            System.out.println("[FileProxy] objectPath: " + objectPath);

            // 根据扩展名设置 Content-Type
            String contentType = URLConnection.guessContentTypeFromName(objectPath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            response.setHeader("Cache-Control", "public, max-age=86400");
            response.setHeader("Access-Control-Allow-Origin", "*");

            try (InputStream in = minioService.getObject(objectPath);
                 OutputStream out = response.getOutputStream()) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[FileProxy] 代理失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
