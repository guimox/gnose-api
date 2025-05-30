package com.gnose.api.dto.user.response;

public class AuthResponseDTO {
    private final String accessToken;
    private final String refreshToken;
    private final String email;

    public AuthResponseDTO(String accessToken, String refreshToken, String email) {
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
