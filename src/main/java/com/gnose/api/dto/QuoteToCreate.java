package com.gnose.api.dto;

import java.time.Instant;

public class QuoteToCreate {
    String quote;
    String hashId;
    Instant timestamp;

    public QuoteToCreate(String quote, String hashId, Instant timestamp) {
        this.quote = quote;
        this.hashId = hashId;
        this.timestamp = timestamp;
    }

    public QuoteToCreate() {
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

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
}
