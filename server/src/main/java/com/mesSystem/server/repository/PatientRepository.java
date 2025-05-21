package com.mesSystem.server.repository;

import com.mesSystem.server.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    // 添加分页查询方法
    Page<Patient> findAll(Pageable pageable);
    Page<Patient> findByNameContaining(String name, Pageable pageable);

    // 根据身份证号查找患者
    Optional<Patient> findByIdCard(String idCard);

    // 检查身份证号是否存在
    boolean existsByIdCard(String idCard);

    // 根据就诊号查找患者
    Optional<Patient> findByMedicalNumber(String medicalNumber);

    // 根据姓名模糊查询患者
    List<Patient> findByNameContaining(String name);

    // 获取当天最大的就诊号
    Optional<Patient> findTopByMedicalNumberStartingWithOrderByMedicalNumberDesc(String prefix);

    boolean existsByMedicalNumber(String medicalNumber);
}