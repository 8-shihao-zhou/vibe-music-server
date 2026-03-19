package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.mapper.ArtistMapper;
import cn.edu.seig.vibemusic.mapper.SongMapper;
import cn.edu.seig.vibemusic.mapper.SongRequestMapper;
import cn.edu.seig.vibemusic.model.dto.SongAddDTO;
import cn.edu.seig.vibemusic.model.dto.SongRequestDTO;
import cn.edu.seig.vibemusic.model.entity.Artist;
import cn.edu.seig.vibemusic.model.entity.Song;
import cn.edu.seig.vibemusic.model.entity.SongRequest;
import cn.edu.seig.vibemusic.model.vo.SongRequestVO;
import cn.edu.seig.vibemusic.result.Result;
import cn.edu.seig.vibemusic.service.INotificationService;
import cn.edu.seig.vibemusic.service.ISongRequestService;
import cn.edu.seig.vibemusic.service.ISongService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SongRequestServiceImpl extends ServiceImpl<SongRequestMapper, SongRequest>
        implements ISongRequestService {

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private ISongService songService;

    @Autowired
    private ArtistMapper artistMapper;

    @Autowired
    private SongMapper songMapper;

    @Override
    public Result submitRequest(SongRequestDTO dto, Long userId) {
        SongRequest entity = new SongRequest();
        BeanUtils.copyProperties(dto, entity);
        entity.setUserId(userId);
        entity.setStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        save(entity);
        return Result.success("提交成功，等待管理员审核");
    }

    @Override
    public Result getUserRequests(Long userId, Integer pageNum, Integer pageSize) {
        Page<SongRequestVO> page = new Page<>(pageNum, pageSize);
        baseMapper.selectUserRequestPage(page, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        return Result.success(data);
    }

    @Override
    public Result adminListRequests(Integer status, String keyword, Integer pageNum, Integer pageSize) {
        Page<SongRequestVO> page = new Page<>(pageNum, pageSize);
        baseMapper.selectRequestPage(page, status, keyword);
        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        return Result.success(data);
    }

    @Override
    @Transactional
    public Result approveRequest(Long id) {
        System.out.println("[SongRequest] 开始审核通过，id=" + id);
        SongRequest request = getById(id);
        if (request == null) return Result.error("请求不存在");
        if (request.getStatus() != 0) return Result.error("该请求已处理");

        System.out.println("[SongRequest] 申请信息: 歌曲=" + request.getSongName()
                + ", 歌手=" + request.getArtistName()
                + ", coverUrl=" + request.getCoverUrl()
                + ", audioUrl=" + request.getAudioUrl());

        // 1. 查找或自动创建歌手
        Long artistId = null;
        if (request.getArtistName() != null && !request.getArtistName().isBlank()) {
            Artist artist = artistMapper.selectOne(
                    new QueryWrapper<Artist>().eq("name", request.getArtistName()).last("LIMIT 1"));
            if (artist == null) {
                System.out.println("[SongRequest] 歌手不存在，自动创建: " + request.getArtistName());
                artist = new Artist();
                artist.setArtistName(request.getArtistName());
                artistMapper.insert(artist);
                System.out.println("[SongRequest] 新歌手ID: " + artist.getArtistId());
            } else {
                System.out.println("[SongRequest] 找到已有歌手: id=" + artist.getArtistId());
            }
            artistId = artist.getArtistId();
        }

        // 2. 构造 SongAddDTO 并调用 addSong
        SongAddDTO songAddDTO = new SongAddDTO();
        songAddDTO.setArtistId(artistId);
        songAddDTO.setSongName(request.getSongName());
        songAddDTO.setAlbum(request.getAlbum() != null ? request.getAlbum() : "");
        songAddDTO.setStyle(request.getStyle() != null ? request.getStyle() : "");
        songAddDTO.setReleaseTime(request.getReleaseTime() != null ? request.getReleaseTime() : java.time.LocalDate.now());
        System.out.println("[SongRequest] 调用 addSong，artistId=" + artistId);

        Result addResult = songService.addSong(songAddDTO);
        System.out.println("[SongRequest] addSong 结果: code=" + addResult.getCode() + ", msg=" + addResult.getMessage());
        if (addResult == null || !addResult.getCode().equals(0)) {
            return Result.error("歌曲导入失败，请手动处理");
        }

        // 3. 拿到 songId，更新封面和音频 URL
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) addResult.getData();
        Object songIdObj = data.get("songId");
        System.out.println("[SongRequest] 新歌曲ID: " + songIdObj);
        if (songIdObj != null) {
            Long songId = Long.valueOf(songIdObj.toString());
            Song song = songMapper.selectById(songId);
            if (song != null) {
                if (request.getCoverUrl() != null && !request.getCoverUrl().isBlank()) {
                    song.setCoverUrl(request.getCoverUrl());
                    System.out.println("[SongRequest] 设置封面URL: " + request.getCoverUrl());
                }
                if (request.getAudioUrl() != null && !request.getAudioUrl().isBlank()) {
                    song.setAudioUrl(request.getAudioUrl());
                    System.out.println("[SongRequest] 设置音频URL: " + request.getAudioUrl());
                    // 使用前端上传时读取的时长
                    String duration = (request.getDuration() != null && !request.getDuration().isBlank())
                            ? request.getDuration() : "0";
                    song.setDuration(duration);
                    System.out.println("[SongRequest] 设置时长: " + duration + "s");
                }
                songMapper.updateById(song);
                System.out.println("[SongRequest] 歌曲URL更新完成");
            }
        }

        // 4. 更新申请状态
        request.setStatus(1);
        request.setUpdateTime(LocalDateTime.now());
        updateById(request);

        // 5. 发通知给用户
        notificationService.createNotificationsEnhanced(
                List.of(request.getUserId()),
                "歌曲收录申请已通过",
                "您提交的歌曲《" + request.getSongName() + "》已通过审核，已自动收录到曲库中，感谢您的贡献！",
                "PERSONAL", "NORMAL", null
        );
        System.out.println("[SongRequest] 审核通过完成");
        return Result.success("已通过并自动导入歌曲库");
    }

    @Override
    @Transactional
    public Result deleteRequests(List<Long> ids) {
        System.out.println("[SongRequest] 批量删除，ids=" + ids);
        removeByIds(ids);
        return Result.success("删除成功");
    }

    @Override
    @Transactional
    public Result rejectRequest(Long id, String reason) {
        SongRequest request = getById(id);
        if (request == null) return Result.error("请求不存在");
        if (request.getStatus() != 0) return Result.error("该请求已处理");

        request.setStatus(2);
        request.setRejectReason(reason);
        request.setUpdateTime(LocalDateTime.now());
        updateById(request);

        // 发通知给用户
        String content = "您提交的歌曲《" + request.getSongName() + "》未能通过审核。";
        if (reason != null && !reason.isBlank()) {
            content += "原因：" + reason;
        }
        notificationService.createNotificationsEnhanced(
                List.of(request.getUserId()),
                "歌曲收录申请未通过",
                content,
                "PERSONAL", "NORMAL", null
        );
        return Result.success("已拒绝");
    }
}
