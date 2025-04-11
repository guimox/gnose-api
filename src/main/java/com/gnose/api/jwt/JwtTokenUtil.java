package com.gnose.api.jwt;

import com.gnose.api.model.User;
import com.gnose.api.web.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${spring.jwt.auth-secret}")
    private String authSecret;

    @Value("${spring.jwt.confirmation-secret}")
    private String confirmationSecret;

    @Value("${spring.jwt.expiration}")
    private Long expiration;

    private static final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000;  // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static final long PASSWORD_RESET_TOKEN_EXPIRY = 10 * 60 * 1000; // 10 minutes for password reset

    private Key getAuthSigningKey() {
        return Keys.hmacShaKeyFor(authSecret.getBytes());
    }

    private Key getConfirmationSigningKey() {
        return Keys.hmacShaKeyFor(confirmationSecret.getBytes());
    }

    public String generateToken(String email, String salt) {
        return Jwts.builder()
                .setSubject(email)
                .claim("salt", salt)
                .claim("tokenType", "auth")
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
                .signWith(getAuthSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email, String salt) {
        return Jwts.builder()
                .setSubject(email)
                .claim("salt", salt)
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY))
                .signWith(getAuthSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSaltFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("salt", String.class), getAuthSigningKey());
    }

    // Generate a confirmation token
    public String generateConfirmationToken(String userEmail) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "confirmation");
        return doGenerateToken(claims, userEmail, getConfirmationSigningKey());
    }

    public boolean validatePasswordResetToken(String token, String userEmail) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(getAuthSigningKey()).build()
                    .parseClaimsJws(token)
                    .getBody();

            // Check if token's email matches the provided email and if it hasn't expired
            return claims.getSubject().equals(userEmail) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Token is invalid or expired
        }
    }

    // Generate a password reset token
    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + PASSWORD_RESET_TOKEN_EXPIRY))
                .signWith(getAuthSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String userEmail, String expectedTokenType, Key signingKey) {
        try {
            Claims claims = getAllClaimsFromToken(token, signingKey);
            return claims.getSubject().equals(userEmail) &&
                    claims.get("tokenType", String.class).equals(expectedTokenType) &&
                    !isTokenExpired(token, signingKey);
        } catch (JwtException e) {
            return false;
        }
    }

    // Validate an authentication token
    public Boolean validateAuthToken(String token, String username) {
        return validateToken(token, username, "auth", getAuthSigningKey());
    }

    // Validate a confirmation token
    public Boolean validateConfirmationToken(String token, String userEmail) {
        return validateToken(token, userEmail, "confirmation", getConfirmationSigningKey());
    }

    // Extract user email from token
    public String getUserEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject, getAuthSigningKey());
    }

    // Get expiration date from the confirmation token
    public Date getExpirationDateFromTokenConfirmation(String token) {
        return getClaimFromToken(token, Claims::getExpiration, getConfirmationSigningKey());
    }

    // Extract claim from token
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver, Key signingKey) {
        Claims claims = getAllClaimsFromToken(token, signingKey);
        return claimsResolver.apply(claims);
    }

    // Get all claims from the token
    private Claims getAllClaimsFromToken(String token, Key signingKey) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration, getAuthSigningKey());
    }

    private Boolean isTokenExpired(String token, Key signingKey) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // Helper method to generate token with specified claims
    private String doGenerateToken(Map<String, Object> claims, String subject, Key signingKey) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }
}
