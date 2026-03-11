-- =============================================
-- 举报系统数据库表
-- 创建时间: 2026-03-11
-- =============================================

-- 举报表
CREATE TABLE IF NOT EXISTS `tb_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_type` TINYINT NOT NULL COMMENT '举报目标类型: 1-帖子, 2-评论',
  `target_id` BIGINT NOT NULL COMMENT '举报目标ID',
  `reason_type` VARCHAR(50) NOT NULL COMMENT '举报原因类型',
  `reason_detail` TEXT COMMENT '举报详细说明',
  `status` TINYINT DEFAULT 0 COMMENT '处理状态: 0-待处理, 1-已处理, 2-已驳回',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
  `handle_result` TEXT COMMENT '处理结果说明',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 举报统计表（用于快速判断是否需要自动处理）
CREATE TABLE IF NOT EXISTS `tb_report_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-帖子, 2-评论',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `report_count` INT DEFAULT 0 COMMENT '举报次数',
  `last_report_time` DATETIME DEFAULT NULL COMMENT '最后举报时间',
  `is_auto_hidden` TINYINT DEFAULT 0 COMMENT '是否已自动隐藏: 0-否, 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_target` (`target_type`, `target_id`),
  KEY `idx_report_count` (`report_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报统计表';

-- 插入举报原因类型配置（可选）
-- 这些可以在代码中定义，也可以存储在数据库中
