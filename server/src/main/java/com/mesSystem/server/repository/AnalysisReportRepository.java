package com.mesSystem.server.repository;

import com.mesSystem.server.model.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, String> {
    
    // 根据患者ID查找所有分析报告
    // 以下方法使用了错误的属性名，应该注释掉或删除
    // List<AnalysisReport> findByPatientId(String patientId);
    
    // 使用正确的属性路径
    List<AnalysisReport> findByPatient_Id(String patientId);
    
    // 根据患者ID和报告类型查找分析报告
    // 以下方法使用了错误的属性名，应该注释掉或删除
    // List<AnalysisReport> findByPatientIdAndReportType(String patientId, String reportType);
    
    // 使用正确的属性路径
    List<AnalysisReport> findByPatient_IdAndReportType(String patientId, String reportType);
    
    // 查找患者最新的分析报告
    Optional<AnalysisReport> findFirstByPatient_IdOrderByCreateTimeDesc(String patientId);
    
    // 查找患者特定类型的最新分析报告
    Optional<AnalysisReport> findFirstByPatient_IdAndReportTypeOrderByCreateTimeDesc(String patientId, String reportType);
    
    // 分页查询患者的分析报告
    // 以下方法使用了错误的属性名，应该注释掉或删除
    // Page<AnalysisReport> findByPatientId(String patientId, Pageable pageable);
    
    // 使用正确的属性路径
    Page<AnalysisReport> findByPatient_Id(String patientId, Pageable pageable);
    
    // 根据报告类型查找分析报告
    List<AnalysisReport> findByReportType(String reportType);
    
    // 删除患者的所有分析报告
    // 以下方法使用了错误的属性名，应该注释掉或删除
    // void deleteByPatientId(String patientId);
    
    // 使用正确的属性路径
    void deleteByPatient_Id(String patientId);
}