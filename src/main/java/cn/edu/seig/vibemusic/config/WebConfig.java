package cn.edu.seig.vibemusic.config;

import cn.edu.seig.vibemusic.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

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

                        // 错误页面不拦截
                        "/error",
                        
                        // 只放行静态文件访问，AI 接口需要登录验证
                        "/files/**"     //放行 D盘视频文件的访问，让前端能播放
                );
    }
}
