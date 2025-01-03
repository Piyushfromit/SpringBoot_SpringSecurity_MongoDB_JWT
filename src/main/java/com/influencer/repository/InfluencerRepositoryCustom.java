package com.influencer.repository;


import com.influencer.entity.Influencer;

import java.util.Optional;

public interface InfluencerRepositoryCustom {

    Optional<Influencer> findInfluencerWithAuthorities(String email);


}
