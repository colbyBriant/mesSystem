package com.mesSystem.server.dto;

import lombok.Data;

@Data
public class CardReadRequest {
    private String deviceId;        // 读卡设备ID
    private String cardData;        // 读取到的原始数据
    private String timestamp;       // 读卡时间戳
}