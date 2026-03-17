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
import java.util.HashSet;
import java.util.List;
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

            // 2. 获取所有.mp4文件
            List<File> mvFiles = Files.list(dirPath)
                    .filter(p -> p.toString().endsWith(".mp4") && p.getFileName().toString().startsWith("mv_"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            System.out.println(">>> [MV同步] 找到MV文件数量: " + mvFiles.size());

            if (mvFiles.isEmpty()) {
                return Result.success("没有找到新的MV文件");
            }

            // 3. 查询数据库已有的MV
            List<UserMv> existingMvs = userMvMapper.selectByUserId(userId, null);
            Set<String> existingUrls = new HashSet<>();
            if (existingMvs != null) {
                existingUrls = existingMvs.stream()
                        .map(UserMv::getMvUrl)
                        .collect(Collectors.toSet());
            }

            System.out.println(">>> [MV同步] 数据库已有MV数量: " + existingUrls.size());

            // 4. 找出未导入的MV
            List<UserMv> newMvs = new ArrayList<>();
            int index = existingMvs != null ? existingMvs.size() + 1 : 1;
            
            for (File mvFile : mvFiles) {
                String fileName = mvFile.getName();
                String mvUrl = "user_" + userId + "/" + fileName;

                if (!existingUrls.contains(mvUrl) && !existingUrls.contains("/" + mvUrl)) {
                    UserMv mv = new UserMv();
                    mv.setUserId(userId);
                    
                    // 生成MV名称
                    String mvName = "测试MV作品" + index;
                    mv.setMvName(mvName);
                    
                    // 设置MV URL（相对路径）
                    mv.setMvUrl(mvUrl);
                    
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
                    index++;
                }
            }

            System.out.println(">>> [MV同步] 需要导入的新MV数量: " + newMvs.size());

            // 5. 批量插入
            if (!newMvs.isEmpty()) {
                for (UserMv mv : newMvs) {
                    userMvMapper.insert(mv);
                }
                return Result.success("同步完成，新增 " + newMvs.size() + " 个MV");
            } else {
                return Result.success("所有MV已同步，无需更新");
            }

        } catch (Exception e) {
            System.err.println(">>> [MV同步] 同步失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error("同步失败：" + e.getMessage());
        }
    }
}
