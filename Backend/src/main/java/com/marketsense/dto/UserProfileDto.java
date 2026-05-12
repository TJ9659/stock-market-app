package com.marketsense.dto;

import com.marketsense.model.User;
import lombok.Data;

@Data
public class UserProfileDto {
    private String email;
    private String name;

    public UserProfileDto(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
