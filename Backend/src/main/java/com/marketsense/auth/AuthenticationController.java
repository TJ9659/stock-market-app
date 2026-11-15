package com.marketsense.auth;

import com.marketsense.auth.dto.LoginUserDto;
import com.marketsense.auth.dto.RegisterUserDto;
import com.marketsense.auth.dto.ResendVerificationRequest;
import com.marketsense.auth.dto.VerifyEmailRequest;
import org.springframework.http.HttpStatus;
import com.marketsense.common.dto.UserProfileDto;
import com.marketsense.common.exceptions.EmailTakenException;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import com.marketsense.auth.responses.LoginResponse;
import com.marketsense.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    private final UserRepository userRepository;

    private final UserService userService;

    private final EmailVerificationService emailVerificationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, UserRepository userRepository, UserService userService,
                                    EmailVerificationService emailVerificationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
    }


    @PostMapping("/register")
    public ResponseEntity<?> signup(@Valid @RequestBody RegisterUserDto registerUserDto, BindingResult result){

        if(userRepository.existsByEmail(registerUserDto.getEmail())){
            throw new EmailTakenException("Email already in use");
        }

        User registeredUser = authenticationService.register(registerUserDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Account created. Check your email to verify your account.",
                "email", registeredUser.getEmail()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return ResponseEntity.ok(Map.of("message", "Email verified. You can now sign in."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest request) {
        emailVerificationService.resend(request.email());
        return ResponseEntity.ok(Map.of("message", "If that account exists and isn't verified yet, we've sent a new link."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUserDto loginUserDto){
        User authenticatedUser = authenticationService.login(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpiration(), new UserProfileDto(authenticatedUser));
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LoginUserDto loginUserDto){
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication){
        User currentUser = (User) authentication.getPrincipal();

        UserProfileDto response = new UserProfileDto(currentUser);

        return ResponseEntity.ok(response);
    }

}
