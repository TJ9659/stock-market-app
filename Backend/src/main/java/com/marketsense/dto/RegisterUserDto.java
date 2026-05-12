package com.marketsense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
//import com.github.tj9659.swiftspend.validation.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//@PasswordMatches
@Data
public class RegisterUserDto {

    @NotBlank(message = "Name can't be blank")
    private String name;

    @NotBlank(message = "Email can't be blank")
    private String email;

    @NotBlank(message = "Password can't be blank")
    private String password;

    @JsonProperty("password_confirmation")

    @NotBlank(message = "Password Confirmation can't be blank")
    private String passwordConfirmation;


}
