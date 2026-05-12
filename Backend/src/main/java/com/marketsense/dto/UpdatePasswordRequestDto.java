package com.marketsense.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
