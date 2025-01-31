package com.gnose.api.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Pattern;

@Component
public class AuthUtils {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public boolean isValidEmailFormat(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public String hashPassword(String password) {
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password cannot be empty");
        return passwordEncoder.encode(password);
    }

    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) return false;
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public boolean isTokenExpired(Date expirationDate) {
        return expirationDate != null && expirationDate.before(new Date());
    }

    public String generateRandomToken(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String maskEmail(String email) {
        if (!isValidEmailFormat(email)) return null;

        int atIndex = email.indexOf("@");
        if (atIndex < 3) return "***" + email.substring(atIndex);

        return email.substring(0, 1) + "*****" + email.substring(atIndex - 1);
    }
}
