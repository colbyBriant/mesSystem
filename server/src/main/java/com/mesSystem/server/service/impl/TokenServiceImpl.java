package com.mesSystem.server.service.impl;

import com.mesSystem.server.service.TokenService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenServiceImpl implements TokenService {
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();  // 修改value类型为String
    
    @Override
    public String generateToken(String userId) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, userId);
        return token;
    }
    
    @Override
    public boolean validateToken(String token) {
        return token != null && tokenStore.containsKey(token);
    }
    
    @Override
    public void removeToken(String token) {
        tokenStore.remove(token);
    }
    
    @Override
    public String getUserIdFromToken(String token) {
        return tokenStore.get(token);
    }
}