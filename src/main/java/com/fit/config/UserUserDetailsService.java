package com.fit.config;

import com.fit.entity.User;
import com.fit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserUserDetailsService implements UserDetailsService {

    private final UserRepository customerRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       // Optional<User> abc = customerRepository.findUserWithAuthorities(username);

        User user = customerRepository.findUserWithAuthorities(username).orElseThrow(() -> new
                UsernameNotFoundException("User details not found for the user: " + username));

        List<GrantedAuthority> authorities = user.getAuthorities().stream().map(authority -> new
                SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }


}
