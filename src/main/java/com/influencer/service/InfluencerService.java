package com.influencer.service;



import com.influencer.entity.ForgetPasswordOtp;
import com.influencer.entity.RegistrationOtp;
import com.influencer.model.RegistrationRequestDTO;

public interface InfluencerService {


    public void registerUser(RegistrationOtp latestRegistrationOtp);

    public RegistrationOtp saveRegistrationOtp(RegistrationRequestDTO registrationOtp, String otp);

    public ForgetPasswordOtp saveForgetPasswordOtp(String email, String otp);

    public ForgetPasswordOtp updateForgetPasswordOtp(ForgetPasswordOtp latestRegistrationOtp, String resetToken);

    public boolean resetForgetPassword(String userEmail, String newPassword);
}