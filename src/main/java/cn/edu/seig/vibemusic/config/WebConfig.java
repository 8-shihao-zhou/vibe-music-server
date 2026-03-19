package cn.edu.seig.vibemusic.config;

import cn.edu.seig.vibemusic.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Value("${ai.storage-path}")
    private String storagePath;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // AI MV文件静态资源映射
        // 确保路径以斜杠结尾
        String mvLocation = storagePath;
        if (!mvLocation.endsWith("/") && !mvLocation.endsWith("\\")) {
            mvLocation += "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + mvLocation);

        // 帖子图片静态资源映射
        String uploadLocation = uploadPath;
        if (!uploadLocation.endsWith("/") && !uploadLocation.endsWith("\\")) {
            uploadLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadLocation);

        System.out.println("=== [WebConfig] 静态资源配置 ===");
        System.out.println("AI MV路径: file:" + mvLocation);
        System.out.println("上传文件路径: file:" + uploadLocation);
        System.out.println("测试URL: http://localhost:8080/files/user_148/mv_03ea48f99395461dabd1e6d506c5eb39.mp4");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录接口和注册接口不拦截
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/admin/login", "/admin/logout", "/admin/register",
                        "/user/login", "/user/logout", "/user/register",
                        "/user/sendVerificationCode", "/user/resetUserPassword",
                        "/banner/getBannerList",
                        "/playlist/getAllPlaylists", "/playlist/getRecommendedPlaylists", "/playlist/getPlaylistDetail/**",
                        "/artist/getAllArtists", "/artist/getArtistDetail/**",
                        "/song/getAllSongs", "/song/getRecommendedSongs", "/song/getSongDetail/**",
                        
                        // 社区接口已在拦截器中处理，支持未登录浏览
                        // 移除了 /community/post/list 和 /community/post/detail/** 的排除
                        // 让它们经过拦截器，以便获取登录用户信息

                        // 搜索接口不拦截（支持未登录用户）
                        "/search/**",

                        // 错误页面不拦截
                        "/error",
                        
                        // 只放行静态文件访问，AI 接口需要登录验证
                        "/files/**",    //放行 D盘视频文件的访问，让前端能播放
                        "/uploads/**",   //放行上传文件的访问（帖子图片）
                        
                        // 文件代理接口（无需登录，供前端直接访问 MinIO 文件）
                        "/file/proxy",

                        // 测试接口
                        "/test/**"
                );
    }
}
