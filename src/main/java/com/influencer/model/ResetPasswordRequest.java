package com.influencer.model;


public record ResetPasswordRequest(String email, String newPassword, String resetToken) {

}
