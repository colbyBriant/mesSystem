package com.mesSystem.server.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageDTO {
    private String type;        // 图片类型（舌象/面色）
    private String imageData;   // Base64编码的图片数据
    private String analysis;    // 图片分析结果
}