package com.fit.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.fit.entity.User;
import com.fit.entity.Authority;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Optional<User> findUserWithAuthorities(String email) {
        // Match user by email
        Query userQuery = new Query(Criteria.where("email").is(email));

        // Fetch the user object
        User user = mongoTemplate.findOne(userQuery, User.class);

        // If user is found, also fetch their authorities
        if (user != null) {
            Query authorityQuery = new Query(Criteria.where("user.$id").is(user.getId()));
            Set<Authority> authorities = new HashSet<>(mongoTemplate.find(authorityQuery, Authority.class));
            user.setAuthorities(authorities);
        }

        return Optional.ofNullable(user);
    }



}
