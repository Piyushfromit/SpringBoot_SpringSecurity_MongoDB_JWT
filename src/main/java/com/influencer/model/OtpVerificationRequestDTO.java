package com.influencer.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerificationRequestDTO(

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{4}", message = "OTP must be a 4-digit number")
    String otp


) {}
