package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.entity.UserMv;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户MV服务接口
 *
 * @author system
 * @since 2026-03-16
 */
public interface IUserMvService extends IService<UserMv> {

    /**
     * 获取用户的MV列表
     *
     * @param userId 用户ID（null表示当前用户）
     * @param status 状态（null表示所有状态）
     * @return MV列表
     */
    Result getUserMvList(Long userId, Integer status);

    /**
     * 检查MV是否属于指定用户
     *
     * @param mvId   MV ID
     * @param userId 用户ID
     * @return 是否属于
     */
    boolean checkMvOwnership(Long mvId, Long userId);

    /**
     * 获取MV详情
     *
     * @param mvId MV ID
     * @return MV详情
     */
    Result getMvDetail(Long mvId);

    /**
     * 同步本地MV文件到数据库
     *
     * @param userId 用户ID（null表示当前用户）
     * @return 同步结果
     */
    Result syncMvFiles(Long userId);
}
