package com.mesSystem.server.controller;

import com.mesSystem.server.dto.AnalysisReportDTO;
import com.mesSystem.server.model.AnalysisReport;
import com.mesSystem.server.model.Patient;
import com.mesSystem.server.service.AnalysisReportService;
import com.mesSystem.server.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analysis-reports")
public class AnalysisReportController {
    
    private final AnalysisReportService analysisReportService;
    private final PatientService patientService;
    
    @Autowired
    public AnalysisReportController(AnalysisReportService analysisReportService, PatientService patientService) {
        this.analysisReportService = analysisReportService;
        this.patientService = patientService;
    }
    
    @PostMapping
    public ResponseEntity<AnalysisReportDTO> createAnalysisReport(@RequestBody AnalysisReportDTO reportDTO) {
        AnalysisReport report = convertToEntity(reportDTO);
        AnalysisReport savedReport = analysisReportService.save(report);
        return ResponseEntity.ok(convertToDTO(savedReport));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisReportDTO> getAnalysisReport(@PathVariable String id) {
        AnalysisReport report = analysisReportService.findById(id);
        return ResponseEntity.ok(convertToDTO(report));
    }
    
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AnalysisReportDTO>> getAnalysisReportsByPatientId(
            @PathVariable String patientId,
            @RequestParam(required = false) String reportType) {
        
        List<AnalysisReport> reports;
        if (reportType != null && !reportType.isEmpty()) {
            reports = analysisReportService.findByPatientIdAndReportType(patientId, reportType);
        } else {
            reports = analysisReportService.findByPatientId(patientId);
        }
        
        List<AnalysisReportDTO> reportDTOs = reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(reportDTOs);
    }
    
    @GetMapping("/patient/{patientId}/latest")
    public ResponseEntity<AnalysisReportDTO> getLatestAnalysisReportByPatientId(
            @PathVariable String patientId,
            @RequestParam(required = false) String reportType) {
        
        Optional<AnalysisReport> latestReport;
        if (reportType != null && !reportType.isEmpty()) {
            latestReport = analysisReportService.findLatestByPatientIdAndReportType(patientId, reportType);
        } else {
            latestReport = analysisReportService.findLatestByPatientId(patientId);
        }
        
        return latestReport
                .map(report -> ResponseEntity.ok(convertToDTO(report)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/patient/{patientId}/page")
    public ResponseEntity<Page<AnalysisReportDTO>> getAnalysisReportsByPatientIdPaged(
            @PathVariable String patientId,
            Pageable pageable) {
        
        Page<AnalysisReport> reportPage = analysisReportService.findByPatientId(patientId, pageable);
        Page<AnalysisReportDTO> reportDTOPage = reportPage.map(this::convertToDTO);
        
        return ResponseEntity.ok(reportDTOPage);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AnalysisReportDTO> updateAnalysisReport(
            @PathVariable String id,
            @RequestBody AnalysisReportDTO reportDTO) {
        
        AnalysisReport report = convertToEntity(reportDTO);
        AnalysisReport updatedReport = analysisReportService.update(id, report);
        
        return ResponseEntity.ok(convertToDTO(updatedReport));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalysisReport(@PathVariable String id) {
        analysisReportService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // DTO 转换方法
    private AnalysisReport convertToEntity(AnalysisReportDTO reportDTO) {
        AnalysisReport report = new AnalysisReport();
        
        if (reportDTO.getId() != null) {
            report.setId(reportDTO.getId());
        }
        
        if (reportDTO.getPatientId() != null) {
            Patient patient = patientService.findById(reportDTO.getPatientId());
            report.setPatient(patient);
        }
        
        report.setReportType(reportDTO.getReportType());
        report.setAnalysisResult(reportDTO.getAnalysisResult());
        
        // 添加体质辨识结果
        report.setConstitutionResult(reportDTO.getConstitutionResult());
        report.setMainConstitutionType(reportDTO.getMainConstitutionType());
        report.setSecondaryConstitutionType(reportDTO.getSecondaryConstitutionType());
        
        // 处理原始图片
        if (reportDTO.getOriginalImage() != null && !reportDTO.getOriginalImage().isEmpty()) {
            String base64Image = reportDTO.getOriginalImage();
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }
            report.setOriginalImage(Base64.getDecoder().decode(base64Image));
        }
        
        // 处理分析图片
        if (reportDTO.getAnalysisImage() != null && !reportDTO.getAnalysisImage().isEmpty()) {
            String base64Image = reportDTO.getAnalysisImage();
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }
            report.setAnalysisImage(Base64.getDecoder().decode(base64Image));
        }
        
        return report;
    }
    
    private AnalysisReportDTO convertToDTO(AnalysisReport report) {
        AnalysisReportDTO reportDTO = new AnalysisReportDTO();
        
        reportDTO.setId(report.getId());
        reportDTO.setPatientId(report.getPatient().getId());
        reportDTO.setReportType(report.getReportType());
        reportDTO.setAnalysisResult(report.getAnalysisResult());
        
        // 添加体质辨识结果
        reportDTO.setConstitutionResult(report.getConstitutionResult());
        reportDTO.setMainConstitutionType(report.getMainConstitutionType());
        reportDTO.setSecondaryConstitutionType(report.getSecondaryConstitutionType());
        
        reportDTO.setCreateTime(report.getCreateTime());
        reportDTO.setUpdateTime(report.getUpdateTime());
        
        // 处理原始图片
        if (report.getOriginalImage() != null) {
            reportDTO.setOriginalImage("data:image/jpeg;base64," + 
                    Base64.getEncoder().encodeToString(report.getOriginalImage()));
        }
        
        // 处理分析图片
        if (report.getAnalysisImage() != null) {
            reportDTO.setAnalysisImage("data:image/jpeg;base64," + 
                    Base64.getEncoder().encodeToString(report.getAnalysisImage()));
        }
        
        return reportDTO;
    }
}