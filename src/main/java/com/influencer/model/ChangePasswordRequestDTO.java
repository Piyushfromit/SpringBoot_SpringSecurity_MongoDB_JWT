package com.influencer.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$",
                message = "Password must be strong"
        )
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        @NotBlank(message = "Password is required")
        String oldPassword,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$",
                message = "Password must be strong"
        )
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        @NotBlank(message = "Password is required")
        String newPassword

) {}
