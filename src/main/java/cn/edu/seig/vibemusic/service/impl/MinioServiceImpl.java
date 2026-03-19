package cn.edu.seig.vibemusic.service.impl;

import cn.edu.seig.vibemusic.constant.MessageConstant;
import cn.edu.seig.vibemusic.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件到 Minio
     *
     * @param file   文件
     * @param folder 文件夹
     * @return 可访问的 URL
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 生成唯一文件名
            String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

            // 获取文件流
            InputStream inputStream = file.getInputStream();

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 返回可访问的 URL
            return endpoint + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + "：" + e.getMessage());
        }
    }

    @Override
    public InputStream getObject(String objectPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件 URL
     */
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 检查 fileUrl 是否为空
            if (fileUrl == null || fileUrl.trim().isEmpty()) {
                System.out.println("警告: 文件 URL 为空，跳过删除操作");
                return;
            }

            // 解析 URL，获取文件路径
            String prefix = endpoint + "/" + bucketName + "/";
            String filePath;
            
            if (fileUrl.startsWith(prefix)) {
                filePath = fileUrl.substring(prefix.length());
            } else if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
                // 如果是完整 URL 但格式不匹配，尝试提取路径部分
                int bucketIndex = fileUrl.indexOf("/" + bucketName + "/");
                if (bucketIndex != -1) {
                    filePath = fileUrl.substring(bucketIndex + bucketName.length() + 2);
                } else {
                    System.out.println("警告: 无法解析文件 URL: " + fileUrl);
                    return;
                }
            } else {
                // 如果不是完整 URL，直接作为文件路径使用
                filePath = fileUrl;
            }

            // 检查文件路径是否有效
            if (filePath.trim().isEmpty()) {
                System.out.println("警告: 解析后的文件路径为空，跳过删除操作");
                return;
            }

            System.out.println("正在删除文件: " + filePath);

            // 删除文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );

            System.out.println("文件删除成功: " + filePath);

        } catch (Exception e) {
            System.err.println("文件删除失败: " + fileUrl);
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件删除失败: " + e.getMessage() + ": cause(" + e.getCause() + ")");
        }
    }
}
