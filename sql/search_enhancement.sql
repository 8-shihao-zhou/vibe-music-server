-- 搜索功能增强 SQL
-- 创建时间: 2026-03-04

-- 1. 搜索历史表
CREATE TABLE IF NOT EXISTS tb_search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    keyword VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    search_type VARCHAR(20) DEFAULT 'POST' COMMENT '搜索类型：POST-帖子, USER-用户',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    KEY idx_user_time (user_id, create_time DESC),
    KEY idx_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史表';

-- 2. 热门搜索统计表
CREATE TABLE IF NOT EXISTS tb_search_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    keyword VARCHAR(100) UNIQUE NOT NULL COMMENT '搜索关键词',
    search_count INT DEFAULT 1 COMMENT '搜索次数',
    last_search_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后搜索时间',
    KEY idx_count (search_count DESC),
    KEY idx_time (last_search_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热门搜索统计表';

-- 3. 为帖子表的标题和内容添加全文索引（可选，提升搜索性能）
-- 注意：如果表中已有大量数据，创建全文索引可能需要较长时间
-- ALTER TABLE tb_community_post ADD FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram;

-- 4. 为帖子表添加搜索权重字段（可选，用于搜索排序）
ALTER TABLE tb_community_post ADD COLUMN IF NOT EXISTS search_weight INT DEFAULT 0 COMMENT '搜索权重';

-- 5. 创建搜索权重更新触发器（可选）
-- 根据点赞数、评论数、浏览数自动计算搜索权重
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS update_search_weight_after_update
AFTER UPDATE ON tb_community_post
FOR EACH ROW
BEGIN
    IF NEW.like_count != OLD.like_count 
       OR NEW.comment_count != OLD.comment_count 
       OR NEW.view_count != OLD.view_count THEN
        UPDATE tb_community_post 
        SET search_weight = (like_count * 3 + comment_count * 5 + view_count * 1)
        WHERE id = NEW.id;
    END IF;
END$$
DELIMITER ;

-- 6. 初始化现有帖子的搜索权重
UPDATE tb_community_post 
SET search_weight = (like_count * 3 + comment_count * 5 + view_count * 1)
WHERE search_weight = 0;
