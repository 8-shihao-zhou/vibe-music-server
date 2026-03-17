package cn.edu.seig.vibemusic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //读取 yml 里配置的 storage-path
    @Value("${ai.storage-path}")
    private String storagePath;

    //读取文件上传路径
    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //关键配置：把URL路径 /files/** 映射到本地磁盘（AI MV文件）
        //这里的 "file:" 前缀是必须的
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + storagePath);

        //配置上传文件的静态资源访问（帖子图片）
        //确保路径以 / 或 \ 结尾
        String uploadLocation = uploadPath;
        if (!uploadLocation.endsWith("/") && !uploadLocation.endsWith("\\")) {
            uploadLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadLocation);
        
        System.out.println("=== 静态资源配置 ===");
        System.out.println("AI MV路径: file:" + storagePath);
        System.out.println("上传文件路径: file:" + uploadLocation);
    }
}