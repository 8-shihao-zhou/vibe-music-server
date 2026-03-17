-- ============================================
-- 帖子媒体增强功能 - 数据库脚本
-- 功能：支持多图上传和MV分享
-- ============================================

-- 1. 创建帖子媒体表
CREATE TABLE IF NOT EXISTS tb_post_media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    media_type TINYINT NOT NULL COMMENT '媒体类型: 1-图片, 2-MV',
    media_url VARCHAR(500) NOT NULL COMMENT '媒体URL',
    media_name VARCHAR(255) COMMENT '媒体名称',
    media_size BIGINT COMMENT '媒体大小(字节)',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_post_id (post_id),
    KEY idx_media_type (media_type),
    KEY idx_sort (post_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子媒体表';

-- 2. 创建用户MV作品表（如果不存在）
CREATE TABLE IF NOT EXISTS tb_user_mv (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    mv_name VARCHAR(255) NOT NULL COMMENT 'MV名称',
    mv_url VARCHAR(500) NOT NULL COMMENT 'MV文件URL',
    cover_url VARCHAR(500) COMMENT 'MV封面URL',
    duration INT COMMENT 'MV时长(秒)',
    file_size BIGINT COMMENT '文件大小(字节)',
    description TEXT COMMENT 'MV描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-生成中, 1-已完成, 2-失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户MV作品表';

-- 3. 修改帖子表，添加媒体统计字段（先检查列是否存在）
-- 添加 image_count 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'tb_community_post' 
  AND COLUMN_NAME = 'image_count';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE tb_community_post ADD COLUMN image_count INT DEFAULT 0 COMMENT ''图片数量'' AFTER cover_url', 
    'SELECT ''Column image_count already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 has_mv 列
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'tb_community_post' 
  AND COLUMN_NAME = 'has_mv';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE tb_community_post ADD COLUMN has_mv TINYINT DEFAULT 0 COMMENT ''是否包含MV: 0-否, 1-是'' AFTER image_count', 
    'SELECT ''Column has_mv already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 创建索引优化查询（先检查索引是否存在）
-- 创建 idx_image_count 索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'tb_community_post' 
  AND INDEX_NAME = 'idx_image_count';

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_image_count ON tb_community_post(image_count)', 
    'SELECT ''Index idx_image_count already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建 idx_has_mv 索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'tb_community_post' 
  AND INDEX_NAME = 'idx_has_mv';

SET @sql = IF(@index_exists = 0, 
    'CREATE INDEX idx_has_mv ON tb_community_post(has_mv)', 
    'SELECT ''Index idx_has_mv already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- 测试数据（可选）
-- ============================================

-- 插入测试MV数据（假设用户ID为148）
INSERT INTO tb_user_mv (user_id, mv_name, mv_url, cover_url, duration, file_size, status) VALUES
(148, '测试MV作品1', '/AI-MusicMV/user_148/mv_03ea48f99395461dabd1e6d506c5eb39.mp4', '/AI-MusicMV/user_148/cover_03ea48f99395461dabd1e6d506c5eb39.jpg', 180, 15728640, 1),
(148, '测试MV作品2', '/AI-MusicMV/user_148/mv_6fa73a39241741e582aca7aa9ea55960.mp4', '/AI-MusicMV/user_148/cover_6fa73a39241741e582aca7aa9ea55960.jpg', 200, 18874368, 1),
(148, '测试MV作品3', '/AI-MusicMV/user_148/mv_828737452e844775ab196521e190be22.mp4', '/AI-MusicMV/user_148/cover_828737452e844775ab196521e190be22.jpg', 150, 12582912, 1)
ON DUPLICATE KEY UPDATE mv_name = VALUES(mv_name);

-- ============================================
-- 说明
-- ============================================
-- 1. tb_post_media: 存储帖子的所有媒体文件（图片和MV）
-- 2. tb_user_mv: 存储用户的MV作品库
-- 3. tb_community_post: 添加媒体统计字段，方便查询和展示
-- 4. 媒体类型: 1-图片, 2-MV
-- 5. sort_order: 用于图片排序显示
