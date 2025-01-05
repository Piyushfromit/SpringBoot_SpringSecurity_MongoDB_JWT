package com.fit.repository;


import com.fit.entity.RegistrationOtp;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RegistrationOtpRepository extends MongoRepository<RegistrationOtp, Integer>{

    List<RegistrationOtp> findAllByEmail(String email);






}
