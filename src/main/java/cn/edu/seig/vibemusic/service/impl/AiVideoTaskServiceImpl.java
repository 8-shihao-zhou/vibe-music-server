package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constants.AiVideoStylePresets;
import cn.edu.seig.vibemusic.model.dto.AiVideoTaskCreateDTO;
import cn.edu.seig.vibemusic.model.entity.AiVideoTask;
import cn.edu.seig.vibemusic.model.vo.AiVideoTaskVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.IAiVideoTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI MV 任务服务实现
 */
@Service
public class AiVideoTaskServiceImpl extends ServiceImpl<cn.edu.seig.vibemusic.mapper.AiVideoTaskMapper, AiVideoTask> implements IAiVideoTaskService {

    @Autowired
    private AiVideoTaskAsyncService aiVideoTaskAsyncService;

    @Value("${ai.storage-path}")
    private String storagePath;

    private static final String FILE_URL_PREFIX = "http://localhost:8080/files/";

    @Override
    public Result<AiVideoTaskVO> createTask(Long userId, AiVideoTaskCreateDTO dto) {
        if (userId == null) {
            return Result.error("请先登录后再发起 MV 创作");
        }
        if (dto == null) {
            return Result.error("任务参数不能为空");
        }
        if (dto.getSongName() == null || dto.getSongName().trim().isEmpty()) {
            return Result.error("歌曲名称不能为空");
        }
        if (dto.getAudioUrl() == null || dto.getAudioUrl().trim().isEmpty()) {
            return Result.error("音频地址不能为空");
        }

        String styleCode = AiVideoStylePresets.normalizeCode(dto.getStyleCode());
        String styleLabel = AiVideoStylePresets.resolveLabel(styleCode, dto.getStyleLabel());

        AiVideoTask task = new AiVideoTask()
                .setUserId(userId)
                .setSongName(dto.getSongName().trim())
                .setArtistName(dto.getArtistName() == null ? null : dto.getArtistName().trim())
                .setAudioUrl(dto.getAudioUrl().trim())
                .setStatus("QUEUED")
                .setStatusText("任务已提交，当前风格：" + styleLabel + "，等待开始生成")
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        save(task);
        aiVideoTaskAsyncService.processTask(task.getId(), styleCode, styleLabel);
        return Result.success(toVO(task));
    }

    @Override
    public Result<List<AiVideoTaskVO>> getCurrentUserTasks(Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        List<AiVideoTask> tasks = list(
                new LambdaQueryWrapper<AiVideoTask>()
                        .eq(AiVideoTask::getUserId, userId)
                        .orderByDesc(AiVideoTask::getCreateTime)
        );

        List<AiVideoTaskVO> taskList = tasks.stream()
                .map(task -> toVO(normalizeTaskMedia(task)))
                .collect(Collectors.toList());

        return Result.success(taskList);
    }

    @Override
    public Result<String> deleteCurrentUserTask(Long userId, Long taskId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        if (taskId == null) {
            return Result.error("任务不存在");
        }

        AiVideoTask task = getOne(new LambdaQueryWrapper<AiVideoTask>()
                .eq(AiVideoTask::getId, taskId)
                .eq(AiVideoTask::getUserId, userId)
                .last("limit 1"));
        if (task == null) {
            return Result.error("未找到对应任务");
        }

        boolean removed = remove(new LambdaQueryWrapper<AiVideoTask>()
                .eq(AiVideoTask::getId, taskId)
                .eq(AiVideoTask::getUserId, userId));
        if (!removed) {
            return Result.error("删除任务失败");
        }

        return Result.success("任务已删除");
    }

    @Override
    public void syncTaskMediaAfterRename(Long userId, String oldFileName, String newFileName) {
        if (userId == null || oldFileName == null || newFileName == null) {
            return;
        }

        List<AiVideoTask> taskList = list(new LambdaQueryWrapper<AiVideoTask>()
                .eq(AiVideoTask::getUserId, userId)
                .eq(AiVideoTask::getStatus, "SUCCESS"));

        String oldUrl = buildFileUrl(userId, oldFileName);
        String newUrl = buildFileUrl(userId, newFileName);
        String newMvName = stripMp4Suffix(newFileName);

        for (AiVideoTask task : taskList) {
            boolean matched = oldFileName.equalsIgnoreCase(safe(task.getMvFileName()))
                    || oldUrl.equals(safe(task.getMvUrl()));
            if (!matched) {
                continue;
            }

            task.setMvFileName(newFileName);
            task.setMvName(newMvName);
            task.setMvUrl(newUrl);
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);
        }
    }

    /**
     * 统一修正任务中的文件地址，避免旧数据中的 mvUrl 失效。
     */
    private AiVideoTask normalizeTaskMedia(AiVideoTask task) {
        if (task == null || !"SUCCESS".equals(task.getStatus())) {
            return task;
        }
        if (task.getUserId() == null || task.getMvFileName() == null || task.getMvFileName().trim().isEmpty()) {
            return task;
        }

        File mvFile = new File(storagePath + "user_" + task.getUserId() + File.separator + task.getMvFileName());
        if (!mvFile.exists()) {
            return task;
        }

        String latestUrl = buildFileUrl(task.getUserId(), task.getMvFileName());
        if (!latestUrl.equals(task.getMvUrl())) {
            task.setMvUrl(latestUrl);
            task.setMvName(stripMp4Suffix(task.getMvFileName()));
            task.setUpdateTime(LocalDateTime.now());
            updateById(task);
        }
        return task;
    }

    private String buildFileUrl(Long userId, String fileName) {
        return FILE_URL_PREFIX + "user_" + userId + "/" + fileName;
    }

    private String stripMp4Suffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        if (fileName.toLowerCase().endsWith(".mp4")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * 转换为前端使用的任务对象
     */
    private AiVideoTaskVO toVO(AiVideoTask task) {
        AiVideoTaskVO vo = new AiVideoTaskVO();
        BeanUtils.copyProperties(task, vo);
        return vo;
    }
}
