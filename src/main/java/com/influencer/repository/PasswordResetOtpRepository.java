package com.influencer.repository;


import com.influencer.entity.ForgetPasswordOtp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PasswordResetOtpRepository extends MongoRepository<ForgetPasswordOtp, Integer> {

    List<ForgetPasswordOtp> findAllByEmail(String email);





}
