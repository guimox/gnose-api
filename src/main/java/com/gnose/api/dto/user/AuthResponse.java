package com.gnose.api.dto.user;

public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final String email;

    public AuthResponse(String accessToken, String refreshToken, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getEmail() {
        return email;
    }
}
