package com.mesSystem.server.exception;

/**
 * 资源未找到异常
 * 
 * @author Tlt
 * @date 2024/1/6
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}