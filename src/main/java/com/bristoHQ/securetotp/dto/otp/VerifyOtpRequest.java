package com.bristoHQ.securetotp.dto.otp;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otp;
}
