package com.mesSystem.server.controller;

import com.mesSystem.server.dto.UserDTO;
import com.mesSystem.server.dto.LoginRequest;
import com.mesSystem.server.dto.LoginResponse;
import com.mesSystem.server.model.User;
import com.mesSystem.server.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
/**
 * 用户管理控制器
 * 
 * @author Tlt
 * @date 2024/3/21
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserDTO userDTO) {
        // 设置默认状态
        if (userDTO.getName() == null) {
            userDTO.setName(userDTO.getUsername());  // 如果没有提供name，使用username作为默认值
        }
        if (userDTO.getStatus() == null) {
            userDTO.setStatus("ACTIVE");  // 设置默认状态
        }
        if (userDTO.getRole() == null) {
            userDTO.setRole("USER");  // 设置默认角色
        }
        User user = convertToEntity(userDTO);
        User savedUser = userService.save(user);
        return ResponseEntity.ok(convertToDTO(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String token) {
        userService.logout(token);
        Map<String, String> response = new HashMap<>();
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(convertToDTO(user));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User updatedUser = userService.update(id, user);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsersByUsername(
            @RequestParam String username) {
        List<User> users = userService.findByUsername(username);
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable String id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("message", "密码修改成功");
        }});
    }

    // DTO 转换方法
    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRole(userDTO.getRole());
        user.setDepartment(userDTO.getDepartment());
        user.setStatus(userDTO.getStatus());
        return user;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setRole(user.getRole());
        userDTO.setDepartment(user.getDepartment());
        userDTO.setStatus(user.getStatus());
        userDTO.setCreateTime(user.getCreateTime());
        userDTO.setLastLoginTime(user.getLastLoginTime());
        return userDTO;
    }
}