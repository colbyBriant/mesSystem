package com.mesSystem.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReportDTO {
    
    private String id;
    private String patientId;
    private String reportType;
    private String originalImage; // Base64 编码的图片
    private String analysisImage; // Base64 编码的图片
    private String analysisResult;
    private String constitutionResult;
    private String mainConstitutionType;
    private String secondaryConstitutionType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}