package com.mesSystem.server.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录数据传输对象
 * 
 * @author Tlt
 * @date 2024/1/6
 */
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
}