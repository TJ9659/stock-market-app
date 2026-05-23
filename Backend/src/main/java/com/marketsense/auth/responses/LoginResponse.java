package com.marketsense.auth.responses;

import com.marketsense.common.dto.UserProfileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private long expiresIn;
    private UserProfileDto user;

}
