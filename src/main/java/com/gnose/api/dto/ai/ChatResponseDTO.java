package com.gnose.api.dto.ai;

import java.util.List;

public class ChatResponseDTO {

    private List<Choice> choices;

    public static class Choice {

        private int index;
        private ChatMessageDTO chatMessageDTO;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public ChatMessageDTO getMessage() {
            return chatMessageDTO;
        }

        public void setMessage(ChatMessageDTO chatMessageDTO) {
            this.chatMessageDTO = chatMessageDTO;
        }
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}