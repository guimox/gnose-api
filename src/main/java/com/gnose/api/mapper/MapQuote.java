package com.gnose.api.mapper;

import com.gnose.api.dto.category.CategoryDTO;
import com.gnose.api.dto.language.LanguageDTO;
import com.gnose.api.dto.quote.QuoteResponseDTO;
import com.gnose.api.dto.quote.QuoteToCreate;
import com.gnose.api.model.Category;
import com.gnose.api.model.Language;
import com.gnose.api.model.Quote;

public class MapQuote {

    public static Quote toEntity(QuoteToCreate quoteToCreate, Language language, Category category) {
        Quote quote = new Quote();
        quote.setQuote(quoteToCreate.getQuote());
        quote.setVotes(0);
        quote.setLanguage(language);
        quote.setCategory(category);

        return quote;
    }

    public static QuoteToCreate toDto(Quote quote) {
        return new QuoteToCreate(
                quote.getQuote(),
                null,
                quote.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                quote.getLanguage().getName(),
                quote.getCategory().getName()
        );
    }

    public static QuoteResponseDTO toResponseDto(Quote quote) {
        QuoteResponseDTO dto = new QuoteResponseDTO();
        dto.setQuote(quote.getQuote());
        dto.setCreatedAt(quote.getCreatedAt());
        dto.setUpdatedAt(quote.getUpdatedAt());

        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setName(quote.getLanguage().getName());
        dto.setLanguage(languageDTO);

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName(quote.getCategory().getName());
        dto.setCategory(categoryDTO);

        return dto;
    }
}
