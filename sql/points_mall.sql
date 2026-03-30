-- 积分商城相关数据库表

-- 1. 商城商品表
CREATE TABLE IF NOT EXISTS tb_mall_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_code VARCHAR(50) NOT NULL UNIQUE COMMENT '商品代码',
    item_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    item_description TEXT COMMENT '商品描述',
    item_price INT NOT NULL COMMENT '商品价格（积分）',
    item_type VARCHAR(20) NOT NULL COMMENT '商品类型：POST_TOP,POST_HIGHLIGHT,AVATAR_FRAME,NICKNAME_COLOR',
    duration_days INT DEFAULT 0 COMMENT '有效期天数（0表示永久）',
    item_status TINYINT DEFAULT 1 COMMENT '商品状态：1-上架，0-下架',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_item_type (item_type),
    INDEX idx_item_status (item_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商城商品表';

-- 2. 用户购买记录表
CREATE TABLE IF NOT EXISTS tb_user_purchase (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    item_id BIGINT NOT NULL COMMENT '商品ID',
    item_code VARCHAR(50) NOT NULL COMMENT '商品代码',
    item_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    points_cost INT NOT NULL COMMENT '消耗积分',
    target_id BIGINT COMMENT '目标ID（如帖子ID）',
    expire_time DATETIME COMMENT '过期时间',
    purchase_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，0-已过期',
    INDEX idx_user_id (user_id),
    INDEX idx_item_code (item_code),
    INDEX idx_target_id (target_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户购买记录表';

-- 3. 用户特权表（头像框、昵称颜色等）
CREATE TABLE IF NOT EXISTS tb_user_privilege (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    privilege_type VARCHAR(20) NOT NULL COMMENT '特权类型：AVATAR_FRAME,NICKNAME_COLOR',
    privilege_value VARCHAR(100) NOT NULL COMMENT '特权值（颜色代码、框样式等）',
    expire_time DATETIME COMMENT '过期时间（NULL表示永久）',
    is_active TINYINT DEFAULT 1 COMMENT '是否激活：1-激活，0-未激活',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_privilege (user_id, privilege_type, privilege_value),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户特权表';

-- 插入默认商品数据（使用英文避免编码问题）
INSERT INTO tb_mall_item (item_code, item_name, item_description, item_price, item_type, duration_days, sort_order) VALUES
('POST_TOP_3D', 'Post Top (3 days)', '将您的帖子置顶显示3天(多个置顶帖子按使用时间倒序排列，越晚使用越靠前)', 50, 'POST_TOP', 3, 1),
('POST_HIGHLIGHT_7D', 'Post Highlight (7 days)', 'Highlight your post for 7 days', 30, 'POST_HIGHLIGHT', 7, 2),
('AVATAR_FRAME_GOLD', 'Gold Avatar Frame', 'Add a gold frame to your avatar', 100, 'AVATAR_FRAME', 0, 3),
('AVATAR_FRAME_RAINBOW', 'Rainbow Avatar Frame', 'Add a rainbow frame to your avatar', 120, 'AVATAR_FRAME', 0, 4),
('NICKNAME_COLOR_RED', 'Red Nickname', 'Display your nickname in red color', 80, 'NICKNAME_COLOR', 0, 5),
('NICKNAME_COLOR_BLUE', 'Blue Nickname', 'Display your nickname in blue color', 80, 'NICKNAME_COLOR', 0, 6),
('NICKNAME_COLOR_PURPLE', 'Purple Nickname', 'Display your nickname in purple color', 80, 'NICKNAME_COLOR', 0, 7),
('NICKNAME_COLOR_GRADIENT', 'Gradient Nickname', 'Display your nickname in gradient color', 150, 'NICKNAME_COLOR', 0, 8),
-- 主页装扮
('PROFILE_THEME_OCEAN', '深海主题', '深邃的深海蓝色主页装扮', 200, 'PROFILE_THEME', 0, 9),
('PROFILE_THEME_SUNSET', '落日主题', '浪漫的粉橙渐变主页装扮', 200, 'PROFILE_THEME', 0, 10),
('PROFILE_THEME_FOREST', '森林主题', '清新的绿色森林主页装扮', 200, 'PROFILE_THEME', 0, 11),
('PROFILE_THEME_AURORA', '极光主题', '绚丽的极光色主页装扮', 300, 'PROFILE_THEME', 0, 12);

-- 帖子装扮商品（执行此 INSERT 添加到数据库）
INSERT INTO tb_mall_item (item_code, item_name, item_description, item_price, item_type, duration_days, sort_order) VALUES
('POST_THEME_STARRY', '星空帖子装扮', '帖子卡片呈现深邃星空，流星划过，繁星闪烁', 180, 'POST_THEME', 0, 13),
('POST_THEME_SAKURA', '樱花帖子装扮', '帖子卡片飘落粉色樱花花瓣，浪漫唯美', 180, 'POST_THEME', 0, 14),
('POST_THEME_NEON', '霓虹帖子装扮', '赛博朋克风格，霓虹扫光线条动态效果', 250, 'POST_THEME', 0, 15),
('POST_THEME_LAVA', '熔岩帖子装扮', '橙红熔岩风格，火花上升，炽热动感', 250, 'POST_THEME', 0, 16);
