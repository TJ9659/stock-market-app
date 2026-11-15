package com.marketsense.auth;

import com.marketsense.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    @Query("SELECT t FROM EmailVerificationToken t JOIN FETCH t.user WHERE t.tokenHash = :tokenHash")
    Optional<EmailVerificationToken> findByTokenHash(@Param("tokenHash") String tokenHash);

    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user = :user")
    void deleteByUser(@Param("user") User user);
}
