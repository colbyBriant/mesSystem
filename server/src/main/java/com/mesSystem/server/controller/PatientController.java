package com.mesSystem.server.controller;

import com.mesSystem.server.dto.PatientDTO;
import com.mesSystem.server.dto.AnalysisReportDTO;
import com.mesSystem.server.model.Patient;
import com.mesSystem.server.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import com.mesSystem.server.model.MedicalInsuranceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import com.mesSystem.server.service.MedicalInsuranceService;
import com.mesSystem.server.dto.CardReadRequest;
import com.mesSystem.server.dto.PatientImageRequest;
import com.mesSystem.server.dto.ErrorResponse;
import java.util.Base64;
import java.util.ArrayList;
import com.mesSystem.server.dto.ImageDTO;
import org.springframework.data.domain.Page;
import com.mesSystem.server.service.ImageService;
import com.mesSystem.server.model.AnalysisReport;
import com.mesSystem.server.service.AnalysisReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.HashMap; // 添加这一行导入语句
/**
 * 患者管理控制器
 * 
 * @author Tlt
 * @date 2024/1/6
 */
@RestController
@RequestMapping("/patients")
public class PatientController {
    private final PatientService patientService;
    private final MedicalInsuranceService medicalInsuranceService;
    private final ImageService imageService;
    private final AnalysisReportService analysisReportService; // 添加分析报告服务

    @Autowired
    public PatientController(PatientService patientService, 
                             MedicalInsuranceService medicalInsuranceService,
                             ImageService imageService,
                             AnalysisReportService analysisReportService) {
        this.patientService = patientService;
        this.medicalInsuranceService = medicalInsuranceService;
        this.imageService = imageService;
        this.analysisReportService = analysisReportService; // 注入服务
    }

    @GetMapping
    public ResponseEntity<Page<PatientDTO>> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Patient> patientPage = patientService.getPatients(page, size);
        Page<PatientDTO> patientDTOPage = patientPage.map(this::convertToDTO);
        return ResponseEntity.ok(patientDTOPage);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PatientDTO>> searchPatients(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Patient> patientPage = patientService.searchPatients(name, page, size);
        Page<PatientDTO> patientDTOPage = patientPage.map(this::convertToDTO);
        return ResponseEntity.ok(patientDTOPage);
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        // 如果就诊号为空，自动生成一个
        if (patientDTO.getMedicalNumber() == null || patientDTO.getMedicalNumber().isEmpty()) {
            // 生成就诊号，例如：MES + 当前时间戳
            patientDTO.setMedicalNumber("MES" + System.currentTimeMillis());
        }
        
        Patient patient = convertToEntity(patientDTO);
        Patient savedPatient = patientService.save(patient);
        return ResponseEntity.ok(convertToDTO(savedPatient));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable String id,
            @Valid @RequestBody PatientDTO patientDTO) {
        Patient patient = convertToEntity(patientDTO);
        Patient updatedPatient = patientService.update(id, patient);
        
        // 转换为DTO时确保包含所有必要信息
        PatientDTO updatedDTO = convertToDTO(updatedPatient);
        return ResponseEntity.ok(updatedDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatient(@PathVariable String id) {  // 修改参数类型
        Patient patient = patientService.findById(id);
        return ResponseEntity.ok(convertToDTO(patient));
    }

    // 修改 getAllPatients 方法的映射路径
    @GetMapping("/all")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<Patient> patients = patientService.findAll();
        List<PatientDTO> patientDTOs = patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(patientDTOs);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable String id) {
        patientService.delete(id);
        // 返回包含code和message的成功响应
        return ResponseEntity.ok(new HashMap<String, Object>() {{
            put("code", 200);
            put("message", "删除成功");
        }});
    }

    @GetMapping("/medical-number/{medicalNumber}")
    public ResponseEntity<PatientDTO> getPatientByMedicalNumber(
            @PathVariable String medicalNumber) {
        Patient patient = patientService.findByMedicalNumber(medicalNumber);
        return ResponseEntity.ok(convertToDTO(patient));
    }

    @GetMapping("/search-by-name")
    public ResponseEntity<List<PatientDTO>> searchPatientsByName(
            @RequestParam String name) {
        List<Patient> patients = patientService.findByName(name);
        List<PatientDTO> patientDTOs = patients.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(patientDTOs);
    }

    // DTO 转换方法
    private Patient convertToEntity(PatientDTO patientDTO) {
        Patient patient = new Patient();
        if (patientDTO.getId() != null) {
            patient.setId(patientDTO.getId());  // 确保ID类型匹配
        }
        patient.setMedicalNumber(patientDTO.getMedicalNumber());
        patient.setName(patientDTO.getName());
        patient.setGender(patientDTO.getGender());
        patient.setAge(patientDTO.getAge());
        patient.setIdCard(patientDTO.getIdCard());
        patient.setPhone(patientDTO.getPhone());
        patient.setAddress(patientDTO.getAddress());
        // 在convertToEntity方法中添加
        patient.setCardNumber(patientDTO.getCardNumber());
        return patient;
    }

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO patientDTO = new PatientDTO();
        patientDTO.setId(patient.getId());
        patientDTO.setMedicalNumber(patient.getMedicalNumber());
        patientDTO.setName(patient.getName());
        patientDTO.setGender(patient.getGender());
        patientDTO.setAge(patient.getAge());
        patientDTO.setIdCard(patient.getIdCard());
        patientDTO.setPhone(patient.getPhone());
        patientDTO.setAddress(patient.getAddress());
        patientDTO.setCardNumber(patient.getCardNumber());
        
        // 可以在这里添加分析报告相关信息，如果PatientDTO中有相关字段
        // 例如：patientDTO.setHasAnalysisReports(!patient.getAnalysisReports().isEmpty());
        
        return patientDTO;
    }

    @PostMapping("/{patientId}/images")
    public ResponseEntity<?> updatePatientImages(
            @PathVariable String patientId,
            @RequestBody PatientImageRequest request) {
        try {
            Patient patient = patientService.findById(patientId);
            if (patient == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("患者不存在"));
            }
    
            // 使用分析报告服务保存图片
            if (request.getTongueImage() != null && !request.getTongueImage().isEmpty()) {
                // 创建舌象分析报告
                AnalysisReport tongueReport = new AnalysisReport();
                tongueReport.setPatient(patient);
                tongueReport.setReportType("TONGUE");
                tongueReport.setAnalysisResult(request.getTongueAnalysis());
                
                // 处理图片数据
                String base64Image = request.getTongueImage();
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }
                tongueReport.setOriginalImage(Base64.getDecoder().decode(base64Image));
                
                // 保存报告
                analysisReportService.save(tongueReport);
            }
    
            // 处理面色图片
            if (request.getFaceImage() != null && !request.getFaceImage().isEmpty()) {
                // 创建面色分析报告
                AnalysisReport faceReport = new AnalysisReport();
                faceReport.setPatient(patient);
                faceReport.setReportType("FACE");
                faceReport.setAnalysisResult(request.getFaceAnalysis());
                
                // 处理图片数据
                String base64Image = request.getFaceImage();
                if (base64Image.contains(",")) {
                    base64Image = base64Image.split(",")[1];
                }
                faceReport.setOriginalImage(Base64.getDecoder().decode(base64Image));
                
                // 保存报告
                analysisReportService.save(faceReport);
            }
    
            return ResponseEntity.ok(convertToDTO(patient));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("图片处理失败：" + e.getMessage()));
        }
    }

    @GetMapping("/{patientId}/images")
    public ResponseEntity<List<ImageDTO>> getPatientImages(
            @PathVariable String patientId,
            @RequestParam(required = false) String type) {
        try {
            Patient patient = patientService.findById(patientId);
            if (patient == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 获取患者的分析报告
            List<AnalysisReport> reports;
            if (type != null && !type.isEmpty()) {
                reports = analysisReportService.findByPatientIdAndReportType(patientId, type.toUpperCase());
            } else {
                reports = analysisReportService.findByPatientId(patientId);
            }
            
            // 转换为ImageDTO
            List<ImageDTO> images = new ArrayList<>();
            for (AnalysisReport report : reports) {
                if (report.getOriginalImage() != null) {
                    images.add(new ImageDTO(
                        report.getReportType().toLowerCase(),
                        Base64.getEncoder().encodeToString(report.getOriginalImage()),
                        report.getAnalysisResult()
                    ));
                }
            }
            
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
   
    /** 通过模型获取分析报告 @param icCardId 身份证号 @return 患者信息*/
    @GetMapping("/scan1")
        public ResponseEntity<?> getModelPicture(@RequestParam String icCardId) {
        try {
            // 调用医院API获取患者信息
            String hospitalApiUrl = "http://198.4.20.9:5000/PatientInformation";
            RestTemplate restTemplate = new RestTemplate();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hospitalApiUrl)
                    .queryParam("ic_card_id", icCardId);
            
            ResponseEntity<String> response = restTemplate.getForEntity(
                    builder.toUriString(), String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.getBody());
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("医院API返回错误: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("获取患者信息失败: " + e.getMessage());
        }
    }

    /** 通过扫码获取患者信息 @param icCardId 身份证号 @return 患者信息*/
    @GetMapping("/scan")
    public ResponseEntity<?> getPatientInfoByScan(@RequestParam String icCardId) {
    try {
        // 调用医院API获取患者信息
        String hospitalApiUrl = "http://198.4.20.9:5000/PatientInformation";
        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hospitalApiUrl)
                .queryParam("ic_card_id", icCardId);
        
        ResponseEntity<String> response = restTemplate.getForEntity(
                builder.toUriString(), String.class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(response.getBody());
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("医院API返回错误: " + response.getStatusCode());
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("获取患者信息失败: " + e.getMessage());
    }
}

    // 添加获取患者最新报告的端点
    @GetMapping("/{patientId}/reports/latest")
    public ResponseEntity<?> getLatestReport(
            @PathVariable String patientId,
            @RequestParam(required = false) String type) {
        try {
            Patient patient = patientService.findById(patientId);
            if (patient == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("患者不存在"));
            }
            
            // 根据类型获取最新报告
            if (type != null && !type.isEmpty()) {
                return analysisReportService.findLatestByPatientIdAndReportType(patientId, type.toUpperCase())
                        .map(report -> {
                            AnalysisReportDTO reportDTO = convertToReportDTO(report);
                            return ResponseEntity.ok(reportDTO);
                        })
                        .orElse(ResponseEntity.notFound().build());
            } else {
                return analysisReportService.findLatestByPatientId(patientId)
                        .map(report -> {
                            AnalysisReportDTO reportDTO = convertToReportDTO(report);
                            return ResponseEntity.ok(reportDTO);
                        })
                        .orElse(ResponseEntity.notFound().build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("获取报告失败: " + e.getMessage()));
        }
    }
    
    // 保存患者报告的端点
    @PostMapping("/{patientId}/reports/save")
    public ResponseEntity<?> saveReport(@PathVariable String patientId) {
        try {
            Patient patient = patientService.findById(patientId);
            if (patient == null) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("患者不存在"));
            }
            
            // 这里可以添加任何需要的报告保存逻辑
            // 例如，可以标记报告为已保存状态，或将报告导出为PDF等
            
            return ResponseEntity.ok().body("报告保存成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("保存报告失败: " + e.getMessage()));
        }
    }
    
    // 辅助方法：转换分析报告为DTO
    private AnalysisReportDTO convertToReportDTO(AnalysisReport report) {
        AnalysisReportDTO dto = new AnalysisReportDTO();
        dto.setId(report.getId());
        dto.setPatientId(report.getPatientId());
        dto.setReportType(report.getReportType());
        dto.setAnalysisResult(report.getAnalysisResult());
        dto.setConstitutionResult(report.getConstitutionResult());
        dto.setMainConstitutionType(report.getMainConstitutionType());
        dto.setSecondaryConstitutionType(report.getSecondaryConstitutionType());
        dto.setCreateTime(report.getCreateTime());
        dto.setUpdateTime(report.getUpdateTime());
        
        // 处理图片数据
        if (report.getOriginalImage() != null) {
            dto.setOriginalImage("data:image/jpeg;base64," + 
                    Base64.getEncoder().encodeToString(report.getOriginalImage()));
        }
        
        if (report.getAnalysisImage() != null) {
            dto.setAnalysisImage("data:image/jpeg;base64," + 
                    Base64.getEncoder().encodeToString(report.getAnalysisImage()));
        }
        
        return dto;
    }

}
