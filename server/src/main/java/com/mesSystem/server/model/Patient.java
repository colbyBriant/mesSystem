package com.mesSystem.server.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Base64;
import com.mesSystem.server.model.AnalysisReport;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
    
    @Id
    private String id;
    
    @Column(name = "medical_number", unique = true, nullable = false)
    private String medicalNumber;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "id_card", unique = true)
    private String idCard;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "card_number")
    private String cardNumber;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisReport> analysisReports = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    // 添加分析报告的便捷方法
    public void addAnalysisReport(AnalysisReport report) {
        analysisReports.add(report);
        report.setPatient(this);
    }
    
    // 移除分析报告的便捷方法
    public void removeAnalysisReport(AnalysisReport report) {
        analysisReports.remove(report);
        report.setPatient(null);
    }
}