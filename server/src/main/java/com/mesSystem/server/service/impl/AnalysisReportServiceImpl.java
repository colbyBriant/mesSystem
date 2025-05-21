package com.mesSystem.server.service.impl;

import com.mesSystem.server.exception.ResourceNotFoundException;
import com.mesSystem.server.model.AnalysisReport;
import com.mesSystem.server.repository.AnalysisReportRepository;
import com.mesSystem.server.service.AnalysisReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalysisReportServiceImpl implements AnalysisReportService {
    
    private final AnalysisReportRepository analysisReportRepository;
    
    @Autowired
    public AnalysisReportServiceImpl(AnalysisReportRepository analysisReportRepository) {
        this.analysisReportRepository = analysisReportRepository;
    }
    
    @Override
    public AnalysisReport save(AnalysisReport report) {
        if (report.getId() == null || report.getId().isEmpty()) {
            report.setId(UUID.randomUUID().toString());
        }
        return analysisReportRepository.save(report);
    }
    
    @Override
    public AnalysisReport findById(String id) {
        return analysisReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("分析报告不存在，ID: " + id));
    }
    
    @Override
    public List<AnalysisReport> findByPatientId(String patientId) {
        return analysisReportRepository.findByPatient_Id(patientId);
    }
    
    @Override
    public List<AnalysisReport> findByPatientIdAndReportType(String patientId, String reportType) {
        return analysisReportRepository.findByPatient_IdAndReportType(patientId, reportType);
    }
    
    @Override
    public Optional<AnalysisReport> findLatestByPatientId(String patientId) {
        return analysisReportRepository.findFirstByPatient_IdOrderByCreateTimeDesc(patientId);
    }
    
    @Override
    public Optional<AnalysisReport> findLatestByPatientIdAndReportType(String patientId, String reportType) {
        return analysisReportRepository.findFirstByPatient_IdAndReportTypeOrderByCreateTimeDesc(patientId, reportType);
    }
    
    @Override
    public Page<AnalysisReport> findByPatientId(String patientId, Pageable pageable) {
        return analysisReportRepository.findByPatient_Id(patientId, pageable);
    }
    
    @Override
    public AnalysisReport update(String id, AnalysisReport report) {
        AnalysisReport existingReport = findById(id);
        
        existingReport.setReportType(report.getReportType());
        existingReport.setOriginalImage(report.getOriginalImage());
        existingReport.setAnalysisImage(report.getAnalysisImage());
        existingReport.setAnalysisResult(report.getAnalysisResult());
        
        // 更新体质辨识结果字段
        existingReport.setConstitutionResult(report.getConstitutionResult());
        existingReport.setMainConstitutionType(report.getMainConstitutionType());
        existingReport.setSecondaryConstitutionType(report.getSecondaryConstitutionType());
        
        return analysisReportRepository.save(existingReport);
    }
    
    @Override
    public void delete(String id) {
        analysisReportRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByPatientId(String patientId) {
        analysisReportRepository.deleteByPatient_Id(patientId);
    }
}