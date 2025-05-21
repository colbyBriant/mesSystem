package com.mesSystem.server.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReport {
    
    @Id
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @Column(name = "report_type")
    private String reportType;
    
    @Lob
    @Column(name = "original_image")
    private byte[] originalImage;
    
    @Lob
    @Column(name = "analysis_image")
    private byte[] analysisImage;
    
    @Column(name = "analysis_result", columnDefinition = "TEXT")
    private String analysisResult;
    
    @Column(name = "constitution_result", columnDefinition = "TEXT")
    private String constitutionResult;
    
    @Column(name = "main_constitution_type")
    private String mainConstitutionType;
    
    @Column(name = "secondary_constitution_type")
    private String secondaryConstitutionType;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    // 获取患者ID的便捷方法，用于DTO转换
    public String getPatientId() {
        return patient != null ? patient.getId() : null;
    }
}