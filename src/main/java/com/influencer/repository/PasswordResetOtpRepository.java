package com.influencer.repository;


import com.influencer.model.PasswordResetOtp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PasswordResetOtpRepository extends MongoRepository<PasswordResetOtp, Integer> {

    List<PasswordResetOtp> findAllByEmail(String email);





}
