package com.influencer.service;



import com.influencer.model.PasswordResetOtp;
import com.influencer.model.RegistrationOtp;

public interface InfluencerService {


    public void registerInfluencer(RegistrationOtp latestRegistrationOtp);

    public RegistrationOtp saveRegOtpToDB(RegistrationOtp registrationOtp, String otp);


    public PasswordResetOtp savePasswordResetOtp(String email, String otp);

    public PasswordResetOtp updatePasswordResetOtp(PasswordResetOtp latestRegistrationOtp, String resetToken);

    public boolean resetInfluencerPwd(String userEmail, String newPassword);
}