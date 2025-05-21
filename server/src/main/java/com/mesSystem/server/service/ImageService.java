package com.mesSystem.server.service;

import com.mesSystem.server.exception.ImageProcessingException;
import com.mesSystem.server.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ImageService {

    @Value("${app.image.storage.path}")
    private String storageBasePath;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ImageService.class);
    private final PatientRepository patientRepository;

    public ImageService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // 修改保存到本地的方法
    // 将 private 改为 public
    public void saveImageToLocal(String patientId, String imageType, String imageData) throws Exception {
        logger.info("开始保存图片到本地，路径: {}", storageBasePath);
        
        // 确保存储目录存在
        File directory = new File(storageBasePath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new ImageProcessingException("无法创建存储目录: " + storageBasePath);
            }
            logger.info("创建存储目录成功");
        }

        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String fileName = String.format("%s_%s_%s.png", 
            imageType.toLowerCase(),
            patientId,
            timestamp);
            
        logger.info("准备保存文件: {}", fileName);

        try {
            // 保存文件
            String base64Data = imageData.substring(imageData.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            String filePath = storageBasePath + File.separator + fileName;
            Files.write(Paths.get(filePath), imageBytes);
            logger.info("文件保存成功: {}", filePath);
        } catch (Exception e) {
            logger.error("保存文件失败", e);
            throw new ImageProcessingException("保存图片失败: " + e.getMessage());
        }
    }

    // 保持原有的 processImageData 方法不变
    // 将 private 改为 public
    public byte[] processImageData(String imageData) {
        try {
            String base64Data = imageData.substring(imageData.indexOf(",") + 1);
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new ImageProcessingException("Invalid image data format");
        }
    }
}