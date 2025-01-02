package com.influencer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
public class DemoController {


        @GetMapping("/")
        public ResponseEntity<String> welcomeMessage() {

            return ResponseEntity.ok("Welcome to Demo Login Signup API project!");

        }


        @GetMapping("/authenticated")
        public String checkAuthenticated(Authentication authentication) {
            if (authentication != null && authentication.isAuthenticated()) { // not necessary to check, as we are using doFilterInternal filter
                String userEmail = authentication.getName();  // Get the authenticated user's email
                List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
                String username = authentication.getName(); // Get the username from the token
                return "User: " + userEmail + " is authenticated! and roles are " + roles.toString();
            }
            return "Unauthorized";

        }



}
