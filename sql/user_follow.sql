-- =============================================
-- 用户关注功能数据库脚本
-- =============================================

-- 1. 创建用户关注表
CREATE TABLE IF NOT EXISTS tb_user_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    follower_id BIGINT NOT NULL COMMENT '关注者ID（粉丝）',
    following_id BIGINT NOT NULL COMMENT '被关注者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    UNIQUE KEY uk_follower_following (follower_id, following_id) COMMENT '唯一索引：防止重复关注',
    KEY idx_follower (follower_id) COMMENT '关注者索引',
    KEY idx_following (following_id) COMMENT '被关注者索引',
    KEY idx_create_time (create_time) COMMENT '时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- 2. 为用户表添加关注数和粉丝数字段（如果不存在）
-- 注意：如果字段已存在会报错，可以忽略
ALTER TABLE tb_user 
ADD COLUMN following_count INT DEFAULT 0 COMMENT '关注数' AFTER user_avatar;

ALTER TABLE tb_user 
ADD COLUMN follower_count INT DEFAULT 0 COMMENT '粉丝数' AFTER following_count;

-- 3. 创建索引（如果不存在）
CREATE INDEX idx_following_count ON tb_user(following_count);
CREATE INDEX idx_follower_count ON tb_user(follower_count);

-- 4. 初始化现有用户的关注数和粉丝数为0（如果需要）
UPDATE tb_user SET following_count = 0 WHERE following_count IS NULL;
UPDATE tb_user SET follower_count = 0 WHERE follower_count IS NULL;
