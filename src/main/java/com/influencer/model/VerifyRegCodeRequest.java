package com.influencer.model;

import lombok.Data;

@Data
public class VerifyRegCodeRequest {

    private String email;
    private String otp;

}
