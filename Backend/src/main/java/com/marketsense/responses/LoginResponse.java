package com.marketsense.responses;

import com.marketsense.dto.UserProfileDto;

public class LoginResponse {
    private String token;
    private long expiresIn;
    private UserProfileDto user;

    public LoginResponse(String token, long expiresIn, UserProfileDto user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
