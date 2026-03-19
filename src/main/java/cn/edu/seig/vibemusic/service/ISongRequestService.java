package cn.edu.seig.vibemusic.service;

import cn.edu.seig.vibemusic.model.dto.SongRequestDTO;
import cn.edu.seig.vibemusic.model.entity.SongRequest;
import cn.edu.seig.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISongRequestService extends IService<SongRequest> {

    /** 用户提交收录请求 */
    Result submitRequest(SongRequestDTO dto, Long userId);

    /** 用户查看自己的请求列表 */
    Result getUserRequests(Long userId, Integer pageNum, Integer pageSize);

    /** 管理端分页查询（可按状态/关键词筛选） */
    Result adminListRequests(Integer status, String keyword, Integer pageNum, Integer pageSize);

    /** 管理端审核：通过 */
    Result approveRequest(Long id);

    /** 管理端审核：拒绝 */
    Result rejectRequest(Long id, String reason);

    /** 管理端批量删除 */
    Result deleteRequests(List<Long> ids);
}
