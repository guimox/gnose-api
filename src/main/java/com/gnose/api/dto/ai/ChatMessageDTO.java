package com.gnose.api.dto.ai;

public class ChatMessageDTO {

    private String role;
    private String content;

    ChatMessageDTO(String role, String content) {
        this.role = role;
        this.content = content;
    }

    ChatMessageDTO() {
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}