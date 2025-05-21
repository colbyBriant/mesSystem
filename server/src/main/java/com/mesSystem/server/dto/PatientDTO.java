package com.mesSystem.server.dto;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * 患者数据传输对象
 * 
 * @author Tlt
 * @date 2024/1/6
 */
@Data
public class PatientDTO {
    private String id;  // 修改为String类型
    
    // 移除 @NotBlank 注解，允许前端提交空值
    private String medicalNumber;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 150, message = "年龄不能大于150")
    private Integer age;

    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$", 
            message = "身份证号格式不正确")
    private String idCard;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String address;

    private String cardNumber;
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}