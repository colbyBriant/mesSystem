package com.mesSystem.server.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MedicalInsuranceInfo {
    private String cardNumber;      // 医保卡号
    private String name;           // 姓名
    private String gender;         // 性别
    private Integer age;          // 年龄
    private String idCard;        // 身份证号
    private String insuranceType; // 医保类型
    private String validThru;     // 有效期
    private BigDecimal balance;   // 账户余额
    private BigDecimal personalPay; // 个人缴费
    private BigDecimal employerPay; // 单位缴费
    private String insuranceLevel;  // 医保等级
    private String lastUsedDate;    // 最后使用日期
    private String status;          // 卡状态
}