package com.influencer.config;

import com.influencer.entity.Influencer;
import com.influencer.repository.InfluencerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InfluencerUserDetailsService implements UserDetailsService {

    private final InfluencerRepository customerRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       // Optional<Influencer> abc = customerRepository.findInfluencerWithAuthorities(username);

        Influencer influencer = customerRepository.findInfluencerWithAuthorities(username).orElseThrow(() -> new
                UsernameNotFoundException("User details not found for the user: " + username));

        List<GrantedAuthority> authorities = influencer.getAuthorities().stream().map(authority -> new
                SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList());
        return new User(influencer.getEmail(), influencer.getPassword(), authorities);
    }


}
