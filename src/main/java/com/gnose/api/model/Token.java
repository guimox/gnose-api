package com.gnose.api.model;

import com.gnose.api.enums.TokenType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String token;
    private TokenType tokenType;
    private LocalDateTime expiryDate;

    public Token() {}

    public Token(User user, String token, LocalDateTime expiryDate) {
        this.user = user;
        this.token = token;
        this.tokenType = TokenType.CONFIRMATION;
        this.expiryDate = expiryDate;
    }

    public Token(User user, String token, LocalDateTime expiryDate, TokenType tokenType) {
        this.user = user;
        this.token = token;
        this.tokenType = tokenType; // Allow specifying the token type (e.g., PASSWORD_RESET)
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
