package com.fit.service;



import com.fit.entity.ForgetPasswordOtp;
import com.fit.entity.RegistrationOtp;
import com.fit.model.ApiResponse;
import com.fit.model.RegistrationRequestDTO;
import org.springframework.http.ResponseEntity;

public interface LoginSignUpService {


    public void registerUser(RegistrationOtp latestRegistrationOtp);

    public RegistrationOtp saveRegistrationOtp(RegistrationRequestDTO registrationOtp, String otp);

    public ForgetPasswordOtp saveForgetPasswordOtp(String email, String otp);

    public ForgetPasswordOtp updateForgetPasswordOtp(ForgetPasswordOtp latestRegistrationOtp, String resetToken);

    public boolean resetForgetPassword(String userEmail, String newPassword);

    public ResponseEntity<ApiResponse> changeUserPassword(String email, String oldPassword, String newPassword) ;



}