package com.marketsense.common.dto;

import com.marketsense.common.enums.Role;
import com.marketsense.user.User;
import lombok.Data;

@Data
public class UserProfileDto {
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean digestEnabled;
    private boolean emailVerified;

    public UserProfileDto(User user) {
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.digestEnabled = user.isDigestEnabled();
        this.emailVerified = user.isEmailVerified();
    }
}