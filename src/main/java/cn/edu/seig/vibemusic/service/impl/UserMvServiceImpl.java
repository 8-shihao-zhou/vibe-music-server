package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.mapper.UserMvMapper;
import cn.edu.seig.vibemusic.model.entity.UserMv;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IUserMvService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户MV服务实现
 *
 * @author system
 * @since 2026-03-16
 */
@Service
public class UserMvServiceImpl extends ServiceImpl<UserMvMapper, UserMv> implements IUserMvService {

    @Autowired
    private UserMvMapper userMvMapper;

    @Value("${file.upload.url-prefix:http://localhost:9000}")
    private String urlPrefix;

    @Value("${ai.storage-path}")
    private String storagePath;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj != null) {
                return Long.valueOf(userIdObj.toString());
            }
        }
        return null;
    }

    /**
     * 转换MV URL为完整访问路径
     */
    private void convertMvUrl(UserMv mv) {
        if (mv != null && mv.getMvUrl() != null && !mv.getMvUrl().startsWith("http")) {
            // 相对路径，转换为完整URL
            String mvUrl = mv.getMvUrl();
            // 移除可能存在的 AI-MusicMV 前缀
            if (mvUrl.startsWith("/AI-MusicMV/")) {
                mvUrl = mvUrl.substring("/AI-MusicMV/".length());
            } else if (mvUrl.startsWith("AI-MusicMV/")) {
                mvUrl = mvUrl.substring("AI-MusicMV/".length());
            }
            mv.setMvUrl(urlPrefix + "/files/" + mvUrl);
        }
        if (mv != null && mv.getCoverUrl() != null && !mv.getCoverUrl().startsWith("http")) {
            // 封面URL也需要转换
            String coverUrl = mv.getCoverUrl();
            // 移除可能存在的 AI-MusicMV 前缀
            if (coverUrl.startsWith("/AI-MusicMV/")) {
                coverUrl = coverUrl.substring("/AI-MusicMV/".length());
            } else if (coverUrl.startsWith("AI-MusicMV/")) {
                coverUrl = coverUrl.substring("AI-MusicMV/".length());
            }
            mv.setCoverUrl(urlPrefix + "/files/" + coverUrl);
        }
    }

    @Override
    public Result getUserMvList(Long userId, Integer status) {
        // 如果没有指定用户ID，使用当前登录用户ID
        if (userId == null) {
            userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
        }

        System.out.println(">>> [UserMvService] 查询用户MV列表, userId: " + userId + ", status: " + status);

        // 查询用户的MV列表
        List<UserMv> mvList = userMvMapper.selectByUserId(userId, status);
        
        System.out.println(">>> [UserMvService] 查询到MV数量: " + (mvList != null ? mvList.size() : 0));
        
        // 转换所有MV的URL
        if (mvList != null) {
            mvList.forEach(this::convertMvUrl);
        }
        
        return Result.success(mvList);
    }

    @Override
    public boolean checkMvOwnership(Long mvId, Long userId) {
        if (mvId == null || userId == null) {
            return false;
        }
        Integer count = userMvMapper.checkMvOwnership(mvId, userId);
        return count != null && count > 0;
    }

    @Override
    public Result getMvDetail(Long mvId) {
        if (mvId == null) {
            return Result.error("MV ID不能为空");
        }

        UserMv mv = userMvMapper.selectById(mvId);
        if (mv == null) {
            return Result.error("MV不存在");
        }

        // 转换MV URL
        convertMvUrl(mv);

        return Result.success(mv);
    }

    @Override
    public Result syncMvFiles(Long userId) {
        // 如果没有指定用户ID，使用当前登录用户ID
        if (userId == null) {
            userId = getCurrentUserId();
            if (userId == null) {
                return Result.error("请先登录");
            }
        }

        try {
            System.out.println(">>> [MV同步] 开始同步用户MV, userId: " + userId);
            
            // 1. 构建用户MV目录路径
            String userMvDir = storagePath + "user_" + userId;
            Path dirPath = Paths.get(userMvDir);
            
            System.out.println(">>> [MV同步] 扫描目录: " + userMvDir);
            
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return Result.error("用户MV目录不存在");
            }

            // 2. 获取所有.mp4文件（不限制文件名前缀）
            List<File> mvFiles = Files.list(dirPath)
                    .filter(p -> p.toString().endsWith(".mp4"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            System.out.println(">>> [MV同步] 找到MV文件数量: " + mvFiles.size());

            // 3. 删除该用户的所有MV记录（完全重新同步）
            List<UserMv> existingMvs = userMvMapper.selectByUserId(userId, null);
            int deletedCount = 0;
            if (existingMvs != null && !existingMvs.isEmpty()) {
                for (UserMv mv : existingMvs) {
                    userMvMapper.deleteById(mv.getId());
                    deletedCount++;
                }
            }
            System.out.println(">>> [MV同步] 删除旧记录数量: " + deletedCount);

            // 4. 重新导入所有MV文件
            List<UserMv> newMvs = new ArrayList<>();
            for (File mvFile : mvFiles) {
                String fileName = mvFile.getName();
                
                UserMv mv = new UserMv();
                mv.setUserId(userId);
                
                // 使用文件名作为MV名称（移除.mp4扩展名）
                String mvName = fileName;
                if (mvName.toLowerCase().endsWith(".mp4")) {
                    mvName = mvName.substring(0, mvName.length() - 4);
                }
                mv.setMvName(mvName);
                
                // 设置MV URL（相对路径）
                mv.setMvUrl("user_" + userId + "/" + fileName);
                
                // 设置封面URL（如果存在）
                String coverFileName = fileName.replace("mv_", "cover_").replace(".mp4", ".jpg");
                File coverFile = new File(mvFile.getParent(), coverFileName);
                if (coverFile.exists()) {
                    mv.setCoverUrl("user_" + userId + "/" + coverFileName);
                }
                
                // 设置文件大小
                mv.setFileSize(mvFile.length());
                
                // 设置默认时长（3分钟）
                mv.setDuration(180);
                
                // 设置状态为已完成
                mv.setStatus(1);
                
                // 设置创建时间
                mv.setCreateTime(LocalDateTime.now());
                mv.setUpdateTime(LocalDateTime.now());
                
                newMvs.add(mv);
            }

            // 5. 批量插入
            int addedCount = 0;
            if (!newMvs.isEmpty()) {
                for (UserMv mv : newMvs) {
                    userMvMapper.insert(mv);
                    addedCount++;
                }
            }

            System.out.println(">>> [MV同步] 新增记录数量: " + addedCount);

            // 6. 返回同步结果
            String message = String.format("同步完成：共 %d 个MV（已清空旧数据并重新导入）", mvFiles.size());
            return Result.success(message);

        } catch (Exception e) {
            System.err.println(">>> [MV同步] 同步失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("同步失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateMvName(Long mvId, String mvName) {
        // 参数校验
        if (mvId == null) {
            return Result.error("MV ID不能为空");
        }
        if (mvName == null || mvName.trim().isEmpty()) {
            return Result.error("MV名称不能为空");
        }
        if (mvName.length() > 100) {
            return Result.error("MV名称不能超过100个字符");
        }

        // 获取当前用户ID
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        // 查询MV是否存在
        UserMv mv = userMvMapper.selectById(mvId);
        if (mv == null) {
            return Result.error("MV不存在");
        }

        // 检查权限：只能修改自己的MV
        if (!mv.getUserId().equals(currentUserId)) {
            return Result.error("无权修改此MV");
        }

        // 更新MV名称
        mv.setMvName(mvName.trim());
        mv.setUpdateTime(LocalDateTime.now());
        
        int result = userMvMapper.updateById(mv);
        if (result > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }
}
