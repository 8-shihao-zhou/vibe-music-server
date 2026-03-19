package cn.edu.seig.vibemusic.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinioService {
    String uploadFile(MultipartFile file, String folder);
    void deleteFile(String fileUrl);
    InputStream getObject(String objectPath);
    String getBucketName();
}
