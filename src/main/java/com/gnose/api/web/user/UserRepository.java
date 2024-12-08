package com.gnose.api.web.user;

import com.gnose.api.model.UserGnose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserGnose, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<UserGnose> findByUsername(String username);
    Optional<UserGnose> findByEmail(String email);
    Optional<UserGnose> findByPasswordResetToken(String passwordResetToken);
    Optional<UserGnose> findByExternalId(UUID externalId);

    Optional<UserGnose> findByConfirmationToken(String token);
}