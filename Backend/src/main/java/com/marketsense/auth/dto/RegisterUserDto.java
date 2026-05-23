package com.marketsense.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
//import com.github.tj9659.swiftspend.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//@PasswordMatches
@Data
public class RegisterUserDto {

    @NotBlank(message = "Last Name can't be blank")
    private String lastName;

    @NotBlank(message = "First Name can't be blank")
    private String firstName;

//    @NotBlank(message = "Role can't be blank")
//    @Enumerated(EnumType.STRING)
//    private Role role;

    @NotBlank(message = "Email can't be blank")
    private String email;

    @NotBlank(message = "Password can't be blank")
    private String password;

    @JsonProperty("password_confirmation")

    @NotBlank(message = "Password Confirmation can't be blank")
    private String passwordConfirmation;


}
