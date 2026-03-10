package cn.edu.seig.vibemusic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync     // 开启异步方法支持
@EnableCaching   // 开启Spring Boot基于注解的缓存管理支持
@SpringBootApplication
public class VibeMusicServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibeMusicServerApplication.class, args);
    }

}
