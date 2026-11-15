package com.marketsense.auth;

import com.marketsense.auth.dto.LoginUserDto;
import com.marketsense.auth.dto.RegisterUserDto;
import com.marketsense.common.exceptions.EmailNotVerifiedException;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private EmailVerificationService emailVerificationService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authenticationManager = mock(AuthenticationManager.class);
        emailVerificationService = mock(EmailVerificationService.class);
        authenticationService = new AuthenticationService(userRepository, new BCryptPasswordEncoder(4), authenticationManager, emailVerificationService);
    }

    @Test
    void registerCreatesUnverifiedUserAndSendsVerificationEmail() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        RegisterUserDto request = new RegisterUserDto();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");
        request.setPassword("secret123");

        User user = authenticationService.register(request);

        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getPassword()).isNotEqualTo("secret123");
        verify(emailVerificationService).createAndSend(user);
    }

    @Test
    void unverifiedUserCannotLogIn() {
        User user = new User("Jane", "Doe", "jane@example.com", "hashed");
        user.setEmailVerified(false);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.login(login("jane@example.com", "secret123")))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void wrongPasswordIsCheckedBeforeVerificationStatus() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login(login("jane@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void verifiedUserCanLogIn() {
        User user = new User("Jane", "Doe", "jane@example.com", "hashed");
        user.setEmailVerified(true);
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        assertThat(authenticationService.login(login("jane@example.com", "secret123"))).isSameAs(user);
    }

    private static LoginUserDto login(String email, String password) {
        LoginUserDto dto = new LoginUserDto();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }
}
