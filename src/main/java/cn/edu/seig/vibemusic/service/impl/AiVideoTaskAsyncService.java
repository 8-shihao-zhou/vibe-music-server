package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constants.AiVideoStylePresets;
import cn.edu.seig.vibemusic.enums.PointsActionType;
import cn.edu.seig.vibemusic.mapper.AiVideoTaskMapper;
import cn.edu.seig.vibemusic.model.entity.AiVideoTask;
import cn.edu.seig.vibemusic.service.AIService;
import cn.edu.seig.vibemusic.service.INotificationService;
import cn.edu.seig.vibemusic.service.IPointsService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * AI MV 异步任务执行服务
 */
@Service
public class AiVideoTaskAsyncService {

    @Autowired
    private AiVideoTaskMapper aiVideoTaskMapper;

    @Autowired
    private AIService aiService;

    @Autowired
    private IPointsService pointsService;

    @Autowired
    private INotificationService notificationService;

    /**
     * 异步处理 AI MV 生成任务
     */
    @Async
    public void processTask(Long taskId, String styleCode, String styleLabel) {
        AiVideoTask task = aiVideoTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        String normalizedStyleCode = AiVideoStylePresets.normalizeCode(styleCode);
        String resolvedStyleLabel = AiVideoStylePresets.resolveLabel(normalizedStyleCode, styleLabel);

        updateTask(
                new LambdaUpdateWrapper<AiVideoTask>()
                        .eq(AiVideoTask::getId, taskId)
                        .set(AiVideoTask::getStatus, "PROCESSING")
                        .set(AiVideoTask::getStatusText, "正在调用模型生成 MV，当前风格：" + resolvedStyleLabel)
                        .set(AiVideoTask::getStartTime, LocalDateTime.now())
                        .set(AiVideoTask::getUpdateTime, LocalDateTime.now())
        );

        try {
            String displaySongName = buildDisplaySongName(task);
            String videoUrl = aiService.generateVideoFromAudioUrl(
                    task.getUserId(),
                    task.getAudioUrl(),
                    displaySongName,
                    normalizedStyleCode
            );
            String mvFileName = extractFileName(videoUrl);
            String mvName = stripMp4Suffix(mvFileName);

            updateTask(
                    new LambdaUpdateWrapper<AiVideoTask>()
                            .eq(AiVideoTask::getId, taskId)
                            .set(AiVideoTask::getStatus, "SUCCESS")
                            .set(AiVideoTask::getStatusText, "MV 已生成完成，当前风格：" + resolvedStyleLabel)
                            .set(AiVideoTask::getMvUrl, videoUrl)
                            .set(AiVideoTask::getMvFileName, mvFileName)
                            .set(AiVideoTask::getMvName, mvName)
                            .set(AiVideoTask::getFinishTime, LocalDateTime.now())
                            .set(AiVideoTask::getUpdateTime, LocalDateTime.now())
            );

            pointsService.addPoints(task.getUserId(), PointsActionType.MV_CREATE.getCode(), null);
            notificationService.createNotificationsEnhanced(
                    Collections.singletonList(task.getUserId()),
                    "AI MV 生成完成",
                    "你提交的 MV 生成任务已经完成，可前往 AI 创作页面查看作品。",
                    "SYSTEM",
                    "IMPORTANT",
                    0L
            );
        } catch (Exception e) {
            String errorMessage = e.getMessage() == null ? "生成失败，请稍后重试" : e.getMessage();

            updateTask(
                    new LambdaUpdateWrapper<AiVideoTask>()
                            .eq(AiVideoTask::getId, taskId)
                            .set(AiVideoTask::getStatus, "FAILED")
                            .set(AiVideoTask::getStatusText, "本次任务生成失败，当前风格：" + resolvedStyleLabel)
                            .set(AiVideoTask::getErrorMessage, errorMessage)
                            .set(AiVideoTask::getFinishTime, LocalDateTime.now())
                            .set(AiVideoTask::getUpdateTime, LocalDateTime.now())
            );

            notificationService.createNotificationsEnhanced(
                    Collections.singletonList(task.getUserId()),
                    "AI MV 生成失败",
                    "你提交的 MV 生成任务未能完成，请稍后重新尝试。",
                    "SYSTEM",
                    "IMPORTANT",
                    0L
            );
        }
    }

    /**
     * 统一更新任务记录
     */
    private void updateTask(LambdaUpdateWrapper<AiVideoTask> updateWrapper) {
        aiVideoTaskMapper.update(null, updateWrapper);
    }

    /**
     * 组合生成时展示的歌曲标题
     */
    private String buildDisplaySongName(AiVideoTask task) {
        if (task.getArtistName() != null && !task.getArtistName().trim().isEmpty()) {
            return task.getSongName() + " - " + task.getArtistName();
        }
        return task.getSongName();
    }

    /**
     * 从 URL 中提取文件名
     */
    private String extractFileName(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            return "";
        }
        int index = videoUrl.lastIndexOf('/');
        return index >= 0 ? videoUrl.substring(index + 1) : videoUrl;
    }

    /**
     * 去掉 mp4 后缀，得到 MV 名称
     */
    private String stripMp4Suffix(String fileName) {
        if (fileName == null) {
            return "";
        }
        if (fileName.toLowerCase().endsWith(".mp4")) {
            return fileName.substring(0, fileName.length() - 4);
        }
        return fileName;
    }
}
