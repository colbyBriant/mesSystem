package com.mesSystem.server.service;

import com.mesSystem.server.model.Patient;

import java.util.List;
import org.springframework.data.domain.Page;
/**
 * 患者服务接口
 * 
 * @author Tlt
 * @date 2024/1/6
 */
public interface PatientService {
    Patient save(Patient patient);
    Patient update(String id, Patient patient);  // 修改参数类型
    void delete(String id);  // 修改参数类型
    Patient findById(String id);  // 修改参数类型
    List<Patient> findAll();
    Patient findByMedicalNumber(String medicalNumber);
    List<Patient> findByName(String name);
    // 添加分页查询方法
    Page<Patient> getPatients(int page, int size);
    Page<Patient> searchPatients(String name, int page, int size);
    /**
     * 从医保卡获取患者信息
     *
     * @param cardNumber 医保卡号
     * @return 患者信息
     */
    Patient getPatientFromMedicalInsurance(String cardNumber);
}