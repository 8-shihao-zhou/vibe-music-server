package cn.edu.seig.vibemusic.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试静态资源配置
 */
@RestController
@RequestMapping("/test")
public class TestStaticResourceController {

    @Value("${ai.storage-path}")
    private String storagePath;

    @Value("${file.upload.path}")
    private String uploadPath;

    @GetMapping("/static-config")
    public Map<String, Object> testStaticConfig() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("storagePath", storagePath);
        result.put("uploadPath", uploadPath);
        
        // 检查目录是否存在
        File storageDir = new File(storagePath);
        result.put("storagePathExists", storageDir.exists());
        result.put("storagePathAbsolute", storageDir.getAbsolutePath());
        
        File uploadDir = new File(uploadPath);
        result.put("uploadPathExists", uploadDir.exists());
        result.put("uploadPathAbsolute", uploadDir.getAbsolutePath());
        
        // 检查MV文件是否存在
        File mvFile = new File(storagePath + "user_148/mv_03ea48f99395461dabd1e6d506c5eb39.mp4");
        result.put("mvFileExists", mvFile.exists());
        result.put("mvFilePath", mvFile.getAbsolutePath());
        
        // 检查路径是否以斜杠结尾
        result.put("storagePathEndsWithSlash", storagePath.endsWith("/") || storagePath.endsWith("\\"));
        result.put("uploadPathEndsWithSlash", uploadPath.endsWith("/") || uploadPath.endsWith("\\"));
        
        return result;
    }
    
    @GetMapping("/test-mv-access")
    public Map<String, Object> testMvAccess() {
        Map<String, Object> result = new HashMap<>();
        
        String mvPath = "user_148/mv_03ea48f99395461dabd1e6d506c5eb39.mp4";
        File mvFile = new File(storagePath, mvPath);
        
        result.put("mvPath", mvPath);
        result.put("fullPath", mvFile.getAbsolutePath());
        result.put("exists", mvFile.exists());
        result.put("canRead", mvFile.canRead());
        result.put("isFile", mvFile.isFile());
        result.put("size", mvFile.length());
        
        result.put("expectedUrl", "http://localhost:8080/files/" + mvPath);
        
        return result;
    }
}
