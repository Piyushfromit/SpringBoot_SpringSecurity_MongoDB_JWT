package com.fit.repository;


import com.fit.entity.User;

import java.util.Optional;

public interface UserRepositoryCustom {

    Optional<User> findUserWithAuthorities(String email);


}
