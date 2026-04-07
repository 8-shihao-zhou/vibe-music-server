package cn.edu.seig.vibemusic.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * AI MV 任务表初始化
 */
@Component
public class AiVideoTaskTableInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 启动时自动补齐 AI MV 任务表
     */
    @PostConstruct
    public void init() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tb_ai_video_task (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    song_name VARCHAR(255) NOT NULL,
                    artist_name VARCHAR(255) NULL,
                    audio_url VARCHAR(1000) NOT NULL,
                    status VARCHAR(32) NOT NULL,
                    status_text VARCHAR(255) NULL,
                    mv_name VARCHAR(255) NULL,
                    mv_file_name VARCHAR(255) NULL,
                    mv_url VARCHAR(1000) NULL,
                    error_message VARCHAR(1000) NULL,
                    start_time DATETIME NULL,
                    finish_time DATETIME NULL,
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL,
                    INDEX idx_ai_video_task_user_id (user_id),
                    INDEX idx_ai_video_task_status (status),
                    INDEX idx_ai_video_task_create_time (create_time)
                )
                """);
    }
}
