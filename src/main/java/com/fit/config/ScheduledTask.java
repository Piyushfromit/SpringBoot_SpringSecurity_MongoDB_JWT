package com.fit.config;


import com.fit.repository.ForgetPasswordOtpRepository;
import com.fit.repository.RegistrationOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {

    @Autowired
    private RegistrationOtpRepository registrationOtpRepository;

    @Autowired
    private ForgetPasswordOtpRepository passwordResetOtpRepository;



    // @Scheduled(cron = "0 0 0 ? * MON", zone = "Asia/Kolkata") // every Monday at 00:00 AM
    // @Scheduled(fixedRate = 604800000)                         // After every 7 days
    @Scheduled(cron = "0 0 0 ? * 1")                             // every Sunday at 00:00 AM
    public void delAllEntryOfRegistrationOtp() {
        registrationOtpRepository.deleteAll();
    }


    @Scheduled(cron = "0 0 0 ? * 1")                             // every Sunday at 00:00 AM
    public void delAllEntryOfPasswordResetOtpRepository() {
        passwordResetOtpRepository.deleteAll();
    }




}
