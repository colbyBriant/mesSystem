package com.mesSystem.server.service.impl;

import com.mesSystem.server.model.Patient;
import com.mesSystem.server.repository.PatientRepository;
import com.mesSystem.server.service.PatientService;
import com.mesSystem.server.exception.ResourceNotFoundException;
import com.mesSystem.server.exception.BusinessException;
import org.springframework.stereotype.Service;

import com.mesSystem.server.repository.AnalysisReportRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import com.mesSystem.server.service.MedicalInsuranceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.UUID;
import com.mesSystem.server.model.AnalysisReport;
@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final MedicalInsuranceService medicalInsuranceService;
    private final AnalysisReportRepository analysisReportRepository;

    @Autowired
    public PatientServiceImpl(PatientRepository patientRepository, MedicalInsuranceService medicalInsuranceService, AnalysisReportRepository analysisReportRepository) {
        this.patientRepository = patientRepository;
        this.medicalInsuranceService = medicalInsuranceService;
        this.analysisReportRepository = analysisReportRepository;
    }

    @Override
    public Patient save(Patient patient) {
        // // 检查身份证是否已存在
        // if (patientRepository.existsByIdCard(patient.getIdCard())) {
        //     throw new BusinessException("PAT001", "该身份证号已存在");
        // }
        // // 检查就诊号是否已存在
        // if (patient.getMedicalNumber() != null && 
        //     patientRepository.existsByMedicalNumber(patient.getMedicalNumber())) {
        //     throw new BusinessException("PAT003", "该就诊号已存在");
        // }
        
        // 生成患者ID
        if (patient.getId() == null || patient.getId().isEmpty()) {
            patient.setId(UUID.randomUUID().toString());
        }
        
        // 生成就诊号
        if (patient.getMedicalNumber() == null) {
            patient.setMedicalNumber(generateMedicalNumber());
        }
        return patientRepository.save(patient);
    }

    @Override
    public Patient update(String id, Patient patient) {
        Patient existingPatient = findById(id);
        
        // 检查并保存现有的分析报告关联
        boolean hasReports = checkHasAnalysisReport(existingPatient);
        
        // 更新字段，添加空值检查
        existingPatient.setName(patient.getName());
        existingPatient.setGender(patient.getGender());
        existingPatient.setAge(patient.getAge());
        existingPatient.setPhone(patient.getPhone());
        existingPatient.setAddress(patient.getAddress());
        existingPatient.setCardNumber(patient.getCardNumber());
        
        // 添加空值检查
        String newIdCard = patient.getIdCard();
        if (newIdCard != null && existingPatient.getIdCard() != null) {
            // 如果修改了身份证号，需要检查新的身份证号是否已存在
            if (!existingPatient.getIdCard().equals(patient.getIdCard()) &&
                patientRepository.existsByIdCard(patient.getIdCard())) {
                throw new BusinessException("PAT001", "该身份证号已存在");
            }
            existingPatient.setIdCard(newIdCard);
        }
        
        // 保存更新后的患者信息
        Patient updatedPatient = patientRepository.save(existingPatient);
        
        // 如果有分析报告，确保关联被保留
        if (hasReports) {
            updatedPatient = findById(id); // 重新加载患者信息以确保包含分析报告
        }
        
        return updatedPatient;
    }

    /**
     * 检查患者是否有分析报告，并确保编辑后保留这些关联
     * 
     * @param patient 患者对象
     * @return 是否存在分析报告
     */
    private boolean checkHasAnalysisReport(Patient patient) {
        List<AnalysisReport> reports = analysisReportRepository.findByPatient_Id(patient.getId());
        return reports != null && !reports.isEmpty();
    }

    @Override
    public void delete(String id) {
        // 删除患者相关的分析报告
        analysisReportRepository.deleteByPatient_Id(id);
        Patient patient = findById(id);
        patientRepository.delete(patient);
    }

    @Override
    public Patient findById(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的患者"));
    }

    @Override
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Override
    public Patient findByMedicalNumber(String medicalNumber) {
        return patientRepository.findByMedicalNumber(medicalNumber)
                .orElseThrow(() -> new ResourceNotFoundException("未找到就诊号为" + medicalNumber + "的患者"));
    }

    @Override
    public List<Patient> findByName(String name) {
        return patientRepository.findByNameContaining(name);
    }

    @Override
    public Patient getPatientFromMedicalInsurance(String cardNumber) {
        // 读取医保卡信息
        com.mesSystem.server.model.MedicalInsuranceInfo insuranceInfo = medicalInsuranceService.readCardInfo(cardNumber);
        if (insuranceInfo == null) {
            throw new BusinessException("MI001", "读取医保卡失败");
        }

        // 检查是否已存在该患者
        Optional<Patient> existingPatient = patientRepository.findByIdCard(insuranceInfo.getIdCard());
        if (existingPatient.isPresent()) {
            return existingPatient.get();
        }

        // 创建新患者
        Patient patient = new Patient();
        patient.setName(insuranceInfo.getName());
        patient.setGender(insuranceInfo.getGender());
        patient.setAge(insuranceInfo.getAge());
        patient.setIdCard(insuranceInfo.getIdCard());
        
        return save(patient);
    }

    private String generateMedicalNumber() {
        try {
            LocalDate today = LocalDate.now();
            String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String prefix = "MN" + dateStr;
            
            Optional<Patient> maxPatient = patientRepository.findTopByMedicalNumberStartingWithOrderByMedicalNumberDesc(prefix);
            String maxNumber = maxPatient.map(Patient::getMedicalNumber)
                    .orElse(prefix + "0000");
            
            int sequence = Integer.parseInt(maxNumber.substring(maxNumber.length() - 4)) + 1;
            if (sequence > 9999) {
                throw new IllegalStateException("当日就诊号已达到上限");
            }
            
            return prefix + String.format("%04d", sequence);
        } catch (NumberFormatException e) {
            throw new BusinessException("PAT002", "生成就诊号失败");
        }
    }

    @Override
    public Page<Patient> getPatients(int page, int size) {
        return patientRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<Patient> searchPatients(String name, int page, int size) {
        return patientRepository.findByNameContaining(name, PageRequest.of(page, size));
    }
}