package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.dto.AiVideoTaskCreateDTO;
import cn.edu.seig.vibemusic.model.entity.AiVideoTask;
import cn.edu.seig.vibemusic.model.vo.AiVideoTaskVO;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * AI MV 任务服务
 */
public interface IAiVideoTaskService extends IService<AiVideoTask> {

    /**
     * 创建 AI MV 生成任务
     */
    Result<AiVideoTaskVO> createTask(Long userId, AiVideoTaskCreateDTO dto);

    /**
     * 获取当前用户任务列表
     */
    Result<List<AiVideoTaskVO>> getCurrentUserTasks(Long userId);

    /**
     * 删除当前用户的任务记录
     */
    Result<String> deleteCurrentUserTask(Long userId, Long taskId);

    /**
     * 在作品重命名后同步更新任务表中的文件信息
     */
    void syncTaskMediaAfterRename(Long userId, String oldFileName, String newFileName);
}
