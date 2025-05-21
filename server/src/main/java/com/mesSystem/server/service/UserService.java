package com.mesSystem.server.service;

import com.mesSystem.server.model.User;
import com.mesSystem.server.dto.LoginResponse;
import java.util.List;

public interface UserService {
    LoginResponse login(String username, String password);
    void logout(String token);
    User save(User user);
    User update(String id, User user);  // 修改参数类型为String
    void delete(String id);  // 修改参数类型为String
    User findById(String id);  // 修改参数类型为String
    List<User> findAll();
    List<User> findByUsername(String username);
    void changePassword(String id, String oldPassword, String newPassword);  // 修改参数类型为String
}