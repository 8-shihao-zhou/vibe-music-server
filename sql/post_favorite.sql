-- 帖子收藏功能数据库表

-- 创建收藏表
CREATE TABLE IF NOT EXISTS tb_post_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_post (user_id, post_id) COMMENT '用户-帖子唯一索引',
    KEY idx_user (user_id) COMMENT '用户索引',
    KEY idx_post (post_id) COMMENT '帖子索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- 在 tb_community_post 表添加收藏数字段
ALTER TABLE tb_community_post ADD COLUMN favorite_count INT DEFAULT 0 COMMENT '收藏数';
