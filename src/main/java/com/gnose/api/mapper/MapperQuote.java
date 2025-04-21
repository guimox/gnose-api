package com.gnose.api.mapper;

import com.gnose.api.dto.category.CategoryDTO;
import com.gnose.api.dto.language.LanguageDTO;
import com.gnose.api.dto.quote.response.QuoteResponseDTO;
import com.gnose.api.dto.quote.request.QuoteToCreateDTO;
import com.gnose.api.model.Category;
import com.gnose.api.model.Language;
import com.gnose.api.model.Quote;

public class MapperQuote {

    public static Quote toEntity(QuoteToCreateDTO quoteToCreateDTO, Language language, Category category) {
        Quote quote = new Quote();
        quote.setQuote(quoteToCreateDTO.getQuote());
        quote.setVotes(0);
        quote.setLanguage(language);
        quote.setCategory(category);

        return quote;
    }

    public static QuoteToCreateDTO toDto(Quote quote) {
        return new QuoteToCreateDTO(
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

        LanguageDTO languageDTO = new LanguageDTO(quote.getLanguage().getName());
        dto.setLanguage(languageDTO);

        CategoryDTO categoryDTO = new CategoryDTO(quote.getCategory().getName());
        dto.setCategory(categoryDTO);

        return dto;
    }
}
