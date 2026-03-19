-- 用户歌曲收录请求表
CREATE TABLE IF NOT EXISTS `tb_song_request` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT       NOT NULL COMMENT '提交用户ID',
  `song_name`     VARCHAR(200) NOT NULL COMMENT '歌曲名称',
  `artist_name`   VARCHAR(200) NOT NULL COMMENT '歌手名称',
  `album`         VARCHAR(200)          COMMENT '专辑名称',
  `style`         VARCHAR(200)          COMMENT '曲风（逗号分隔）',
  `release_time`  DATE                  COMMENT '发行日期',
  `cover_url`     VARCHAR(500)          COMMENT '封面图URL',
  `audio_url`     VARCHAR(500) NOT NULL COMMENT '音频文件URL',
  `license_desc`  VARCHAR(500) NOT NULL COMMENT '版权说明（开源协议/来源）',
  `remark`        VARCHAR(500)          COMMENT '备注',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `reject_reason` VARCHAR(500)          COMMENT '拒绝原因',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户歌曲收录请求';
