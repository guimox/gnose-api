package com.gnose.api.dto.quote;

public class QuoteResponse {
    private final String correctedQuote;
    private final String category;
    private final String language;

    public QuoteResponse(String correctedQuote, String category, String language) {
        this.correctedQuote = correctedQuote;
        this.category = category;
        this.language = language;
    }

    public String getCorrectedQuote() {
        return correctedQuote;
    }

    public String getCategory() {
        return category;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "QuoteResponse{" +
                "correctedQuote='" + correctedQuote + '\'' +
                ", category='" + category + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
