package com.mesSystem.server.model;

public class ImageRequest {
    private String imageData;
    private String imageType;  // 添加图片类型字段
    private String patientId;  // 添加患者ID字段

    // 添加相应的 getter 和 setter
    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}