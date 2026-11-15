package com.marketsense.auth;

import com.marketsense.auth.dto.RegisterUserDto;
import com.marketsense.config.SecurityConfiguration;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import com.marketsense.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer tests for the auth endpoints, running through the real SecurityConfiguration. */
@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfiguration.class)
class AuthenticationControllerTest {

    private static final String REGISTER_BODY = """
            {"firstName":"Jane","lastName":"Doe","email":"jane@example.com",
             "password":"secret123","password_confirmation":"secret123"}""";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean AuthenticationService authenticationService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean UserService userService;
    @MockitoBean AuthenticationProvider authenticationProvider;
    @MockitoBean UserDetailsService userDetailsService;

    @Test
    void registerIsPublicAndReturnsTokenAndProfile() throws Exception {
        User saved = new User("Jane", "Doe", "jane@example.com", "hashed");
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(authenticationService.register(any(RegisterUserDto.class))).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("jwt-token");
        when(jwtService.getExpiration()).thenReturn(604800000L);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.expiresIn").value(604800000L))
                .andExpect(jsonPath("$.user.email").value("jane@example.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void registerWithTakenEmailReturns409OnEmailField() throws Exception {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.field").value("email"));

        verify(authenticationService, never()).register(any());
    }

    @Test
    void loginWithBadCredentialsReturns401() throws Exception {
        when(authenticationService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.description").value("The username or password is incorrect"));
    }

    @Test
    void currentUserRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void watchlistRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/watchlist")).andExpect(status().isUnauthorized());
    }

    @Test
    void aiInsightRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/insight/AAPL")).andExpect(status().isUnauthorized());
    }
}
