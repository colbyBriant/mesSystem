package com.mesSystem.server.service;

public interface TokenService {
    String generateToken(String userId);  // 修改参数类型为String
    String getUserIdFromToken(String token);
    boolean validateToken(String token);
    void removeToken(String token);
}