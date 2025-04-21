package com.gnose.api.dto.quote.response;

import com.gnose.api.dto.category.CategoryDTO;
import com.gnose.api.dto.language.LanguageDTO;

import java.time.LocalDateTime;

public class QuoteResponseDTO {
    private String quote;
    private int votes;
    private LanguageDTO language;
    private CategoryDTO category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QuoteResponseDTO(String quote, String category, String language) {
        this.quote = quote;
        this.category = new CategoryDTO(category);
        this.language = new LanguageDTO(language);
    }

    public QuoteResponseDTO() {
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public LanguageDTO getLanguage() {
        return language;
    }

    public void setLanguage(LanguageDTO language) {
        this.language = language;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

