package com.influencer.service;



import com.influencer.entity.ForgetPasswordOtp;
import com.influencer.entity.RegistrationOtp;
import com.influencer.model.ApiResponse;
import com.influencer.model.RegistrationRequestDTO;
import org.springframework.http.ResponseEntity;

public interface LoginSignUpService {


    public void registerUser(RegistrationOtp latestRegistrationOtp);

    public RegistrationOtp saveRegistrationOtp(RegistrationRequestDTO registrationOtp, String otp);

    public ForgetPasswordOtp saveForgetPasswordOtp(String email, String otp);

    public ForgetPasswordOtp updateForgetPasswordOtp(ForgetPasswordOtp latestRegistrationOtp, String resetToken);

    public boolean resetForgetPassword(String userEmail, String newPassword);

    public ResponseEntity<ApiResponse> changeInfluencerPassword(String email, String oldPassword, String newPassword) ;



}