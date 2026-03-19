# AI Music Server 🎶

## 介绍 📖

**AI Music Server** 是 AI Music 项目的后端 API 服务。本项目基于 **Spring Boot 3** 构建，采用 **Java 17**、**Maven**、**MyBatis-Plus**、**MySQL**、**Redis** 和 **MinIO** 等技术，为 AI Music 的客户端和管理端提供稳定、高效的数据支持和业务逻辑处理。

## 主要功能 ✨

本服务提供以下核心功能 API：

- **用户认证与管理**: 提供用户注册、登录、信息修改、头像上传、注销等接口，支持管理员对用户进行管理（查询、禁用/启用）。
- **内容管理**:
    - **歌手管理**: 添加、编辑、删除歌手信息。
    - **歌曲管理**: 添加、编辑、删除歌曲信息，处理歌曲文件上传。
    - **歌单管理**: 创建、编辑、删除歌单，管理歌单歌曲。
    - **轮播图管理**: 添加、编辑、删除首页轮播图。
- **用户互动**:
    - **评论管理**: 发表、查看、删除歌曲或歌单的评论。
    - **收藏管理**: 用户收藏/取消收藏歌曲、歌单。
    - **反馈管理**: 提交、查看、处理用户反馈。
- **文件服务**: 使用 MinIO 存储和管理音乐文件、图片（如头像、封面）等静态资源。
- **权限控制**: 基于 JWT 和角色进行 API 访问权限控制。
- **数据缓存**: 利用 Redis 缓存热点数据，提高访问速度。
- **邮件服务**: 支持发送验证码等邮件通知。

## 技术栈 🛠️

- **后端框架**: [Spring Boot 3](https://spring.io/projects/spring-boot)
- **开发语言**: [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **构建工具**: [Maven](https://maven.apache.org/)
- **数据库**: [MySQL](https://www.mysql.com/) (推荐 8.0+)
- **ORM**: [MyBatis-Plus](https://baomidou.com/)
- **缓存**: [Redis](https://redis.io/)
- **对象存储**: [MinIO](https://min.io/)
- **认证**: [JWT (java-jwt)](https://github.com/auth0/java-jwt)
- **数据库连接池**: [Druid](https://github.com/alibaba/druid)
- **工具库**: Lombok, Spring Boot Validation, Java Mail

## 系统需求 ⚙️

- **JDK**: `17` 或更高版本
- **Maven**: `3.6` 或更高版本
- **MySQL**: `8.0` 或更高版本
- **Redis**: 推荐 `6.0` 或更高版本
- **MinIO**: 最新稳定版

## 安装与启动 🚀

1.  **环境准备**

    - 确保已安装并运行 **MySQL 8.0+** 数据库服务。
    - 创建名为 `vibe_music` 的数据库，并使用 `UTF-8` 字符集。
        ```sql
        CREATE DATABASE vibe_music CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
        ```
    - 确保已安装并运行 **Redis** 服务。
    - 确保已安装并运行 **MinIO** 服务。
    - 在 MinIO 中创建一个名为 `vibe-music-data` 的 Bucket，并确保服务具有读写权限。

2.  **配置应用**

    - 找到并修改 `src/main/resources/application.yml` 文件。
    - **数据库配置**: 修改 `spring.datasource` 下的 `url`, `username`, `password`。
    - **Redis 配置**: 修改 `spring.data.redis` 下的 `host`, `port`, `password`。
    - **MinIO 配置**: 修改 `minio` 下的 `endpoint`, `accessKey`, `secretKey`, `bucket`。
    - **邮件服务配置 (可选)**: 修改 `spring.mail` 下的 `host`, `username`, `password`。

    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://YOUR_MYSQL_HOST:3306/vibe_music?useUnicode=true&characterEncoding=utf-8&useSSL=false
        username: YOUR_MYSQL_USER
        password: YOUR_MYSQL_PASSWORD

      data:
        redis:
          host: YOUR_REDIS_HOST
          port: 6379
          password: YOUR_REDIS_PASSWORD
          database: 1

      mail:
        host: smtp.example.com
        username: your-email@example.com
        password: YOUR_EMAIL_APP_PASSWORD

    minio:
      endpoint: http://YOUR_MINIO_HOST:9000
      accessKey: YOUR_MINIO_ACCESS_KEY
      secretKey: YOUR_MINIO_SECRET_KEY
      bucket: vibe-music-data
    ```

3.  **构建项目**

    ```bash
    mvn clean package -DskipTests
    ```

4.  **运行服务**

    ```bash
    java -jar target/ai-music-server-*.jar
    ```
    服务默认启动在 `8080` 端口。

## 依赖服务说明 🔗

- **MySQL**: 用于持久化存储核心业务数据。
- **Redis**: 用于数据缓存，提升性能。
- **MinIO**: 用于存储音乐文件、图片等静态资源。

## 免责声明 ⚠️

本项目仅供学习和技术研究使用，请勿用于任何商业用途。请在遵守相关法律法规及版权政策的前提下使用。

## 许可证 📄

本项目采用 MIT 许可证。
