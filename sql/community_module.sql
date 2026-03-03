-- =============================================
-- 社区模块数据库表
-- 创建时间: 2026-02-08
-- =============================================

-- 1. 社区帖子表
CREATE TABLE IF NOT EXISTS `tb_community_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '发帖用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '帖子标题',
  `content` TEXT NOT NULL COMMENT '帖子内容',
  `category` VARCHAR(50) NOT NULL DEFAULT 'SHARE' COMMENT '帖子分类: SHARE-创作分享, TECH-技术交流, QA-问答互助, CHAT-灌水闲聊',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签(JSON数组格式)',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否, 1-是',
  `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门: 0-否, 1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布, 2-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- 2. 帖子点赞表
CREATE TABLE IF NOT EXISTS `tb_post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞表';

-- 3. 扩展评论表 (如果 tb_comment 表已存在，则执行 ALTER，否则创建新表)
-- 先检查表是否存在，如果不存在则创建
CREATE TABLE IF NOT EXISTS `tb_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `comment_type` TINYINT DEFAULT 0 COMMENT '评论类型: 0-歌曲, 1-歌单, 2-AI-MV作品, 3-社区帖子',
  `target_id` BIGINT NOT NULL COMMENT '目标ID(根据type不同指向不同表)',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID(用于回复)',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target` (`comment_type`, `target_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 如果 tb_comment 表已存在但缺少新字段，执行以下 ALTER 语句
-- 注意：如果字段已存在会报错，这是正常的，可以忽略

-- 添加 comment_type 字段（如果不存在）
ALTER TABLE `tb_comment` 
ADD COLUMN IF NOT EXISTS `comment_type` TINYINT DEFAULT 0 
COMMENT '评论类型: 0-歌曲, 1-歌单, 2-AI-MV作品, 3-社区帖子';

-- 添加 target_id 字段（如果不存在）
ALTER TABLE `tb_comment` 
ADD COLUMN IF NOT EXISTS `target_id` BIGINT 
COMMENT '目标ID(根据type不同指向不同表)';

-- 添加 parent_id 字段（如果不存在）
ALTER TABLE `tb_comment` 
ADD COLUMN IF NOT EXISTS `parent_id` BIGINT DEFAULT 0 
COMMENT '父评论ID(用于回复)';

-- like_count 字段已存在，跳过

-- 4. 评论点赞表
CREATE TABLE IF NOT EXISTS `tb_comment_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `comment_id` BIGINT NOT NULL COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 插入测试数据（可选）
-- INSERT INTO `tb_community_post` (`user_id`, `title`, `content`, `category`, `tags`) VALUES
-- (1, '我的第一个AI-MV作品分享', '大家好，这是我用AI生成的第一个MV作品，欢迎大家点评！', 'SHARE', '["AI-MV", "新手", "分享"]'),
-- (1, 'AI-MV生成技巧分享', '今天给大家分享一些AI-MV生成的小技巧...', 'TECH', '["技巧", "教程"]'),
-- (2, '如何提高MV生成质量？', '请问有什么方法可以提高AI生成MV的质量吗？', 'QA', '["问答", "质量"]');
