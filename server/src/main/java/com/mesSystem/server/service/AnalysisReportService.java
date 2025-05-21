package com.mesSystem.server.service;

import com.mesSystem.server.model.AnalysisReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AnalysisReportService {
    
    // 保存分析报告
    AnalysisReport save(AnalysisReport report);
    
    // 根据ID查找分析报告
    AnalysisReport findById(String id);
    
    // 根据患者ID查找所有分析报告
    List<AnalysisReport> findByPatientId(String patientId);
    
    // 根据患者ID和报告类型查找分析报告
    List<AnalysisReport> findByPatientIdAndReportType(String patientId, String reportType);
    
    // 根据患者ID查找最新的分析报告
    Optional<AnalysisReport> findLatestByPatientId(String patientId);
    
    // 根据患者ID和报告类型查找最新的分析报告
    Optional<AnalysisReport> findLatestByPatientIdAndReportType(String patientId, String reportType);
    
    // 分页查询患者的分析报告
    Page<AnalysisReport> findByPatientId(String patientId, Pageable pageable);
    
    // 更新分析报告
    AnalysisReport update(String id, AnalysisReport report);
    
    // 删除分析报告
    void delete(String id);
    
    // 删除患者的所有分析报告
    void deleteByPatientId(String patientId);
}