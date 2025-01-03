package com.influencer.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.influencer.entity.Influencer;
import com.influencer.entity.Authority;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

@Repository
public class InfluencerRepositoryCustomImpl implements InfluencerRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Optional<Influencer> findInfluencerWithAuthorities(String email) {
        // Match influencer by email
        Query influencerQuery = new Query(Criteria.where("email").is(email));

        // Fetch the influencer object
        Influencer influencer = mongoTemplate.findOne(influencerQuery, Influencer.class);

        // If influencer is found, also fetch their authorities
        if (influencer != null) {
            Query authorityQuery = new Query(Criteria.where("influencer.$id").is(influencer.getId()));
            Set<Authority> authorities = new HashSet<>(mongoTemplate.find(authorityQuery, Authority.class));
            influencer.setAuthorities(authorities);
        }

        return Optional.ofNullable(influencer);
    }



}
