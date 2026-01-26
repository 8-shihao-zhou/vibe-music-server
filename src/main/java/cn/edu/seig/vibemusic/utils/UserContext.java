package cn.edu.seig.vibemusic.utils;

import cn.edu.seig.vibemusic.constant.JwtClaimsConstant;
import cn.edu.seig.vibemusic.util.ThreadLocalUtil;

import java.util.Map;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户的信息
 */
public class UserContext {

    /**
     * 获取当前登录用户的ID
     * @return 用户ID，如果未登录则返回null
     */
    public static Long getUserId() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) {
            return null;
        }
        Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
        if (userIdObj == null) {
            return null;
        }
        // 处理不同类型的userId
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            return Long.parseLong((String) userIdObj);
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户名
     * @return 用户名，如果未登录则返回null
     */
    public static String getUsername() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) {
            return null;
        }
        return (String) claims.get(JwtClaimsConstant.USERNAME);
    }

    /**
     * 获取当前登录用户的邮箱
     * @return 邮箱，如果未登录则返回null
     */
    public static String getEmail() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) {
            return null;
        }
        return (String) claims.get(JwtClaimsConstant.EMAIL);
    }

    /**
     * 获取当前登录用户的角色
     * @return 角色，如果未登录则返回null
     */
    public static String getRole() {
        Map<String, Object> claims = ThreadLocalUtil.get();
        if (claims == null) {
            return null;
        }
        return (String) claims.get(JwtClaimsConstant.ROLE);
    }
}
