package cn.edu.seig.vibemusic.interceptor;


import cn.edu.seig.vibemusic.config.RolePermissionManager;
import cn.edu.seig.vibemusic.constant.JwtClaimsConstant;
import cn.edu.seig.vibemusic.constant.MessageConstant;
import cn.edu.seig.vibemusic.constant.PathConstant;
import cn.edu.seig.vibemusic.util.JwtUtil;
import cn.edu.seig.vibemusic.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RolePermissionManager rolePermissionManager;

    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8"); // 设置字符编码为UTF-8
        response.setContentType("application/json;charset=UTF-8"); // 设置响应的Content-Type
        response.getWriter().write(message);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许 CORS 预检请求（OPTIONS 方法）直接通过
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true; // 直接放行，确保 CORS 预检请求不会被拦截
        }

        String token = request.getHeader("Authorization");
        String path = request.getRequestURI();
        
        System.out.println(">>> [拦截器] 请求路径: " + path);
        System.out.println(">>> [拦截器] Authorization 头: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "null"));

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
            System.out.println(">>> [拦截器] 提取的 token: " + token.substring(0, Math.min(20, token.length())) + "...");
        }
        
        // 获取 Spring 的 PathMatcher 实例
        PathMatcher pathMatcher = new AntPathMatcher();

        // 定义允许访问的路径
        List<String> allowedPaths = Arrays.asList(
                PathConstant.PLAYLIST_DETAIL_PATH,
                PathConstant.ARTIST_DETAIL_PATH,
                PathConstant.SONG_LIST_PATH,
                PathConstant.SONG_DETAIL_PATH,
                "/community/post/list",      // 社区帖子列表
                "/community/post/detail/**"  // 社区帖子详情
        );

        // 检查路径是否匹配
        boolean isAllowedPath = allowedPaths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (token == null || token.isEmpty()) {
            if (isAllowedPath) {
                return true; // 允许未登录用户访问这些路径
            }

            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN); // 缺少令牌
            return false;
        }

        try {
            // 从redis中获取相同的token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);
            System.out.println(">>> [拦截器] Redis 中的 token: " + (redisToken != null ? "存在" : "不存在"));
            
            if (redisToken == null) {
                // token失效
                System.out.println(">>> [拦截器] Token 在 Redis 中不存在，可能已过期");
                throw new RuntimeException();
            }

            Map<String, Object> claims = JwtUtil.parseToken(token);
            System.out.println(">>> [拦截器] 解析 token 成功，claims: " + claims);
            
            String role = (String) claims.get(JwtClaimsConstant.ROLE);
            String requestURI = request.getRequestURI();
            
            System.out.println(">>> [拦截器] 用户角色: " + role);
            System.out.println(">>> [拦截器] 请求 URI: " + requestURI);

            if (rolePermissionManager.hasPermission(role, requestURI)) {
                // 把业务数据存储到ThreadLocal中
                ThreadLocalUtil.set(claims);
                
                // 同时存入 Request Attribute（解决 ThreadLocal 线程切换问题）
                request.setAttribute("userId", claims.get(JwtClaimsConstant.USER_ID));
                request.setAttribute("username", claims.get(JwtClaimsConstant.USERNAME));
                request.setAttribute("email", claims.get(JwtClaimsConstant.EMAIL));
                request.setAttribute("role", claims.get(JwtClaimsConstant.ROLE));
                
                System.out.println(">>> [拦截器] 权限检查通过，已设置 ThreadLocal 和 Request Attribute，userId: " + claims.get(JwtClaimsConstant.USER_ID));
                return true;
            } else {
                System.out.println(">>> [拦截器] 权限检查失败，角色 " + role + " 无权访问 " + requestURI);
                sendErrorResponse(response, 403, MessageConstant.NO_PERMISSION); // 无权限访问
                return false;
            }
        } catch (Exception e) {
            System.out.println(">>> [拦截器] Token 验证失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED); // 令牌无效
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal中的数据
        System.out.println(">>> [拦截器] afterCompletion() - 清除 ThreadLocal，路径: " + request.getRequestURI());
        ThreadLocalUtil.remove();
    }
}
