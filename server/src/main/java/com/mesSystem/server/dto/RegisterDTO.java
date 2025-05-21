package com.mesSystem.server.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "姓名不能为空")  // 添加姓名字段
    private String name;
    
    @NotBlank(message = "角色不能为空")
    private String role;
}