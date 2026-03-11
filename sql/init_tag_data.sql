-- =============================================
-- 初始化标签数据
-- 用于测试标签系统功能
-- =============================================

-- 1. 首先确保标签相关表已创建
-- 如果还没有执行过 tag_system.sql，请先执行它

-- 2. 清空现有测试数据（可选）
-- TRUNCATE TABLE tb_post_tag;
-- TRUNCATE TABLE tb_tag_stats;

-- 3. 插入一些常用标签到统计表
INSERT INTO `tb_tag_stats` (`tag_name`, `use_count`, `post_count`, `last_used_time`) VALUES
('AI-MV', 5, 5, NOW()),
('新手', 3, 3, NOW()),
('分享', 8, 8, NOW()),
('技巧', 4, 4, NOW()),
('教程', 6, 6, NOW()),
('问答', 2, 2, NOW()),
('质量', 3, 3, NOW()),
('创作', 7, 7, NOW()),
('音乐', 10, 10, NOW()),
('视频', 5, 5, NOW()),
('推荐', 4, 4, NOW()),
('求助', 2, 2, NOW()),
('讨论', 6, 6, NOW()),
('经验', 5, 5, NOW()),
('工具', 3, 3, NOW())
ON DUPLICATE KEY UPDATE 
    `use_count` = VALUES(`use_count`),
    `post_count` = VALUES(`post_count`),
    `last_used_time` = VALUES(`last_used_time`);

-- 4. 如果你的帖子表中已有帖子，可以手动为它们添加标签关联
-- 示例：为帖子ID为1的帖子添加标签
-- INSERT INTO `tb_post_tag` (`post_id`, `tag_name`) VALUES
-- (1, 'AI-MV'),
-- (1, '分享'),
-- (1, '新手');

-- 5. 更新现有帖子的tags字段（如果需要）
-- UPDATE `tb_community_post` SET `tags` = '["AI-MV","分享","新手"]' WHERE `id` = 1;
-- UPDATE `tb_community_post` SET `tags` = '["教程","技巧","音乐"]' WHERE `id` = 2;

SELECT '标签数据初始化完成！' AS message;
SELECT COUNT(*) AS tag_count FROM tb_tag_stats;
