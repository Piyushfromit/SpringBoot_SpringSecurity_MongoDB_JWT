package com.influencer.service;



import com.influencer.model.RegistrationOtp;

public interface InfluencerService {


    public void registerInfluencer(RegistrationOtp latestRegistrationOtp);

    public RegistrationOtp saveRegOtpToDB(RegistrationOtp registrationOtp, String otp);





}