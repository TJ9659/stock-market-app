package com.marketsense.controller;


import com.marketsense.dto.UpdatePasswordRequestDto;
import com.marketsense.dto.UpdateProfileRequestDto;
import com.marketsense.dto.UserProfileDto;
import com.marketsense.model.User;
import com.marketsense.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/user")
    public ResponseEntity<?> getUser(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new UserProfileDto(currentUser));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateUserDetails(
            @Valid @RequestBody UpdateProfileRequestDto request,
            Authentication authentication
    ) {
        UserProfileDto updatedUser = userService.updateUserDetails(authentication.getName(), request);

        return ResponseEntity.ok(updatedUser);
    }


    @PutMapping("/me/change-password")
    public ResponseEntity<UserProfileDto> changePassword(@RequestBody UpdatePasswordRequestDto request, Authentication authentication){
        UserProfileDto updatedUser = userService.updateUserPassword(authentication.getName(), request);
        return ResponseEntity.ok(updatedUser);
    }

}
