package com.gnose.api.model;

import com.gnose.api.model.config.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends BaseModel {
    @NotBlank(message = "Name is required")
    @Column(unique = true, nullable = false)
    private String name;

    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "auth_salt")
    private String authSalt;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.authSalt == null) {
            this.authSalt = UUID.randomUUID().toString();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAuthSalt() {
        return authSalt;
    }

    public void setAuthSalt(String authSalt) {
        this.authSalt = authSalt;
    }
}