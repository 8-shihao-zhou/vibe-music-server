-- =============================================
-- 标签系统数据库表
-- 创建时间: 2026-03-11
-- =============================================

-- 1. 标签统计表（用于快速查询热门标签）
CREATE TABLE IF NOT EXISTS `tb_tag_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签统计ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `use_count` INT DEFAULT 0 COMMENT '使用次数',
  `post_count` INT DEFAULT 0 COMMENT '帖子数量',
  `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`),
  KEY `idx_use_count` (`use_count`),
  KEY `idx_post_count` (`post_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签统计表';

-- 2. 帖子标签关联表（用于精确查询和统计）
CREATE TABLE IF NOT EXISTS `tb_post_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_name`),
  KEY `idx_tag_name` (`tag_name`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子标签关联表';

-- 3. 从现有帖子中提取标签数据（初始化数据）
-- 注意：这个脚本会解析 tb_community_post 表中的 tags 字段（JSON格式）
-- 并填充到 tb_post_tag 和 tb_tag_stats 表中

-- 清空现有数据（如果需要重新初始化）
-- TRUNCATE TABLE tb_post_tag;
-- TRUNCATE TABLE tb_tag_stats;

-- 插入一些常用标签到统计表（可选，用于初始化）
INSERT IGNORE INTO `tb_tag_stats` (`tag_name`, `use_count`, `post_count`) VALUES
('AI-MV', 0, 0),
('新手', 0, 0),
('分享', 0, 0),
('技巧', 0, 0),
('教程', 0, 0),
('问答', 0, 0),
('质量', 0, 0),
('创作', 0, 0),
('音乐', 0, 0),
('视频', 0, 0);
