package com.gnose.api.dto.ai;

import java.util.ArrayList;
import java.util.List;

public class ChatRequestDTO {
    private String model;
    private List<ChatMessageDTO> chatMessageDTOS;

    public ChatRequestDTO(String model, String prompt) {
        this.model = model;

        this.chatMessageDTOS = new ArrayList<>();
        this.chatMessageDTOS.add(new ChatMessageDTO("user", prompt));
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<ChatMessageDTO> getMessages() {
        return chatMessageDTOS;
    }

    public void setMessages(List<ChatMessageDTO> chatMessageDTOS) {
        this.chatMessageDTOS = chatMessageDTOS;
    }
}