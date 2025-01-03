package com.influencer.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgetPasswordRequest(

        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        String email


) {}
