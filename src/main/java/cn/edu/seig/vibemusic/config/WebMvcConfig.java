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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //关键配置：把URL路径 /files/** 映射到本地磁盘
        //这里的 "file:" 前缀是必须的
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + storagePath);
    }
}