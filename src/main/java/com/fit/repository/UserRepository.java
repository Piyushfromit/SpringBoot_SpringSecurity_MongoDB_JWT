package com.fit.repository;

import com.fit.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, Integer>, UserRepositoryCustom {

    Optional<User> findByEmail(String email);


 }
