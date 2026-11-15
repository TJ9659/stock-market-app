package com.marketsense.digest;

import com.marketsense.auth.JwtService;
import com.marketsense.common.exceptions.EmptyWatchlistException;
import com.marketsense.config.SecurityConfiguration;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DigestController.class)
@Import({SecurityConfiguration.class, UnsubscribeTokenService.class, DigestConfiguration.class})
@TestPropertySource(properties = {
        "security.jwt.secret-key=test-secret",
        "digest.enabled=false",
        "digest.frontend-url=http://web.test"
})
class DigestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UnsubscribeTokenService tokens;

    @MockitoBean DigestService digestService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserDetailsService userDetailsService;
    @MockitoBean AuthenticationProvider authenticationProvider;

    private final User user = DigestServiceTest.user(5L, "jane@example.com");

    @Test
    void unsubscribeIsPublicAndTurnsTheDigestOff() throws Exception {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/digest/unsubscribe").param("uid", "5").param("sig", tokens.sign(5L)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("You're unsubscribed")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("http://web.test/profile")));

        assertThat(user.isDigestEnabled()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void oneClickPostUnsubscribeAlsoWorks() throws Exception {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/digest/unsubscribe").param("uid", "5").param("sig", tokens.sign(5L)))
                .andExpect(status().isOk());

        assertThat(user.isDigestEnabled()).isFalse();
    }

    @Test
    void forgedSignatureIsRejected() throws Exception {
        mockMvc.perform(get("/api/digest/unsubscribe").param("uid", "5").param("sig", tokens.sign(6L)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid link")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void missingParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/digest/unsubscribe")).andExpect(status().isBadRequest());
    }

    @Test
    void testDigestRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/digest/test")).andExpect(status().isUnauthorized());
        verify(digestService, never()).sendToUser(any());
    }

    @Test
    void testDigestSendsToTheLoggedInUser() throws Exception {
        when(digestService.sendToUser(5L)).thenReturn(3);

        mockMvc.perform(post("/api/digest/test")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sent").value(true))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.stocks").value(3));
    }

    @Test
    void testDigestWithEmptyWatchlistReturns400() throws Exception {
        when(digestService.sendToUser(5L)).thenThrow(new EmptyWatchlistException("Add at least one stock"));

        mockMvc.perform(post("/api/digest/test")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("watchlist"));
    }
}
