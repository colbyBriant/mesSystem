package com.mesSystem.server.service.impl;

import com.mesSystem.server.model.User;
import com.mesSystem.server.repository.UserRepository;
import com.mesSystem.server.service.UserService;
import com.mesSystem.server.service.TokenService;
import com.mesSystem.server.exception.ResourceNotFoundException;
import com.mesSystem.server.exception.BusinessException;
import com.mesSystem.server.dto.LoginResponse;
import com.mesSystem.server.dto.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserServiceImpl(UserRepository userRepository, 
                         PasswordEncoder passwordEncoder,
                         TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("AUTH001", "用户名或密码错误"));

        boolean matched = false;
        if (user.getPassword().equals(password)) {
            // 如果是未加密的初始密码，则进行加密并更新
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
            userRepository.save(user);
            matched = true;
        } else {
            matched = passwordEncoder.matches(password, user.getPassword());
        }

        if (!matched) {
            throw new BusinessException("AUTH001", "用户名或密码错误");
        }

        // 3. 更新登录时间
        user.setLastLoginTime(LocalDateTime.now());
        user = userRepository.save(user);  // 保存并获取更新后的用户对象
        
        // 4. 转换为DTO，确保所有必要字段都被设置
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setName(user.getName());
        userDTO.setRole(user.getRole());  // 直接使用用户角色，不设置默认值
        
        // 5. 生成token
        String token = tokenService.generateToken(user.getId());
        
        // 6. 创建并返回登录响应
        LoginResponse response = new LoginResponse(token, userDTO);
        System.out.println("Login response: " + response);  // 添加日志
        return response;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            tokenService.removeToken(token);
        } else {
            throw new BusinessException("AUTH002", "无效的token格式");
        }
    }

    @Override
    public User save(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("USER001", "用户名已存在");
        }
        
        // 生成用户ID
        String userId = "U" + String.format("%03d", getNextUserId());
        user.setId(userId);
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }
    
    // 添加获取下一个用户ID的方法
    private Long getNextUserId() {
        return userRepository.count() + 1;
    }

    @Override
    public User update(String id, User user) {  // 修改参数类型为String
        User existingUser = findById(id);
        
        if (!existingUser.getUsername().equals(user.getUsername()) &&
            userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("USER001", "用户名已存在");
        }
        
        user.setPassword(existingUser.getPassword());
        user.setId(id);
        
        return userRepository.save(user);
    }

    @Override
    public void delete(String id) {  // 修改参数类型为String
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("未找到ID为" + id + "的用户");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User findById(String id) {  // 修改参数类型为String
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的用户"));
    }

    @Override
    public void changePassword(String id, String oldPassword, String newPassword) {  // 修改参数类型为String
        User user = findById(id);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("USER002", "原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findByUsername(String username) {
        return userRepository.findByUsernameContaining(username);
    }
}