package com.mesSystem.server.service.impl;

import com.mesSystem.server.model.MedicalInsuranceInfo;
import org.springframework.stereotype.Service;
import com.mesSystem.server.service.MedicalInsuranceService;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * 医保卡服务实现类
 * 
 * @author Tlt
 * @date 2024/1/6
 */
@Service
public class MedicalInsuranceServiceImpl implements MedicalInsuranceService {
    @Override
    public MedicalInsuranceInfo readCardInfo(String cardData) {
        // 模拟医保卡读取
        MedicalInsuranceInfo info = new MedicalInsuranceInfo();
        info.setCardNumber("330100" + cardData);  // 模拟卡号生成
        info.setName("张三");
        info.setGender("男");
        info.setAge(30);
        info.setIdCard("110101199001011234");
        info.setInsuranceType("1");  // 职工医保
        info.setValidThru("2025-12-31");
        info.setBalance(new BigDecimal("5000.00"));
        info.setPersonalPay(new BigDecimal("2000.00"));
        info.setEmployerPay(new BigDecimal("8000.00"));
        info.setInsuranceLevel("一级");
        info.setLastUsedDate("2024-01-05");
        info.setStatus("NORMAL");
        
        return info;
    }

    @Override
    public boolean verifyCardInfo(MedicalInsuranceInfo info) {
        // 模拟医保卡验证逻辑
        if (info == null) {
            return false;
        }
        
        // 验证卡片状态
        if (!"NORMAL".equals(info.getStatus())) {
            return false;
        }
        
        // 验证有效期
        try {
            LocalDate validThru = LocalDate.parse(info.getValidThru());
            if (validThru.isBefore(LocalDate.now())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        // 验证余额
        if (info.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        return true;
    }
}