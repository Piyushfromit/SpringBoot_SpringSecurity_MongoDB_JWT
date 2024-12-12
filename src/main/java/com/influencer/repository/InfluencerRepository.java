package com.influencer.repository;

import com.influencer.model.Influencer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfluencerRepository extends MongoRepository<Influencer, Integer>, InfluencerRepositoryCustom {

    Optional<Influencer> findByEmail(String email);


 }
