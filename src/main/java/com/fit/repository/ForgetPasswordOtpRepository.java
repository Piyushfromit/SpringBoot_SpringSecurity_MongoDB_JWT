package com.fit.repository;


import com.fit.entity.ForgetPasswordOtp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ForgetPasswordOtpRepository extends MongoRepository<ForgetPasswordOtp, Integer> {

    List<ForgetPasswordOtp> findAllByEmail(String email);





}
