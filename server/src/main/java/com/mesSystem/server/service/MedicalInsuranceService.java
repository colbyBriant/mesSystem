package com.mesSystem.server.service;

import com.mesSystem.server.model.MedicalInsuranceInfo;

public interface MedicalInsuranceService {
    MedicalInsuranceInfo readCardInfo(String cardData);
    boolean verifyCardInfo(MedicalInsuranceInfo info);
}