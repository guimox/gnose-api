package com.gnose.api.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.security.config.Elements.JWT;

@Component
public class JwtTokenUtil {
    @Value("${jwt.auth-secret}")
    private String authSecret;

    @Value("${jwt.confirmation-secret}")
    private String confirmationSecret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAuthToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "auth"); // Add token type to the claims
        return doGenerateToken(claims, username);
    }

    public String generateConfirmationToken(String userEmail) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "confirmation"); // Explicitly set token type
        return doGenerateToken(claims, userEmail);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // General method to validate tokens (for other use cases)
    public Boolean validateToken(String token, String username, String expectedTokenType) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            return (claims.getSubject().equals(username) &&
                    claims.get("tokenType", String.class).equals(expectedTokenType) &&
                    !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Dedicated method for validating confirmation tokens
    public Boolean validateConfirmationToken(String token, String username) {
        return validateToken(token, username, "confirmation");
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
}