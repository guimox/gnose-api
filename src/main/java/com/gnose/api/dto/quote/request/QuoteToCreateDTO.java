package com.gnose.api.dto.quote.request;

import java.time.Instant;

public class QuoteToCreateDTO {
    private String quote;
    private String hashId;
    private Instant timestamp;
    private String language;
    private String category;

    public QuoteToCreateDTO(String quote, String hashId, Instant timestamp, String language, String category) {
        this.quote = quote;
        this.hashId = hashId;
        this.timestamp = timestamp;
        this.language = language;
        this.category = category;
    }

    public QuoteToCreateDTO() {}

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getHashId() {
        return hashId;
    }

    public void setHashId(String hashId) {
        this.hashId = hashId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
