package com.mesSystem.server.dto;

import lombok.Data;

@Data
public class PatientImageRequest {
    private String tongueImage;      // Base64格式的舌象图片
    private String tongueAnalysis;   // 舌象分析结果
    private String faceImage;        // Base64格式的面色图片
    private String faceAnalysis;     // 面色分析结果
}