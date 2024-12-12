package com.influencer.config;


import com.influencer.repository.RegistrationOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask {

    @Autowired
    private RegistrationOtpRepository registrationOtpRepository;

    // @Scheduled(cron = "0 0 0 ? * MON", zone = "Asia/Kolkata") // every Monday at 00:00 AM
    // @Scheduled(fixedRate = 604800000)                         // After every 7 days
    @Scheduled(cron = "0 0 0 ? * 1")                             // every Sunday at 00:00 AM
    public void delAllEntryOfRegistrationOtp() {
        registrationOtpRepository.deleteAll();
    }




}
