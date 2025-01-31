package com.gnose.api.web.user;

import com.gnose.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPasswordResetToken(String passwordResetToken);
    Optional<User> findByExternalId(UUID externalId);

    Optional<User> findByConfirmationToken(String token);
}