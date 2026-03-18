-- 积分系统数据库表

-- 1. 用户积分表
CREATE TABLE IF NOT EXISTS tb_user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_points INT DEFAULT 0 COMMENT '总积分（累计获得）',
    available_points INT DEFAULT 0 COMMENT '可用积分（当前可使用）',
    level INT DEFAULT 1 COMMENT '用户等级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_level (level),
    INDEX idx_available_points (available_points)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

-- 2. 积分记录表
CREATE TABLE IF NOT EXISTS tb_points_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变化（正数为增加，负数为减少）',
    action_type VARCHAR(50) NOT NULL COMMENT '行为类型',
    description VARCHAR(200) COMMENT '描述',
    related_id BIGINT COMMENT '关联ID（如帖子ID、评论ID）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 3. 每日积分限制表
CREATE TABLE IF NOT EXISTS tb_daily_points_limit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    action_type VARCHAR(50) NOT NULL COMMENT '行为类型',
    points_earned INT DEFAULT 0 COMMENT '今日已获得积分',
    action_count INT DEFAULT 0 COMMENT '今日行为次数',
    date DATE NOT NULL COMMENT '日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_action_date (user_id, action_type, date),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日积分限制表';
