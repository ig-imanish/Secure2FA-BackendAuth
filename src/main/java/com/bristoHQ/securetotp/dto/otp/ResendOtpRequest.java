package com.bristoHQ.securetotp.dto.otp;

import lombok.Data;

@Data
public class ResendOtpRequest {
    private String email;
}
