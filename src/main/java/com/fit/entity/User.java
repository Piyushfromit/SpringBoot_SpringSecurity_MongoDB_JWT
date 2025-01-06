package com.fit.entity;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user")
public class User {

    @Id
    private Long id;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String phone;

    private String status; // ACTIVE, INACTIVE, BANNED
    private LocalDateTime registrationDate; // When the user was registered
    private LocalDateTime lastLoginDate;    // Last login timestamp

    @DBRef(lazy = true) // Use lazy loading for better performance
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Set<Authority> authorities = new HashSet<>(); // Initialize to avoid null references



}




