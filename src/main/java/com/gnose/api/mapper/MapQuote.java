package com.gnose.api.mapper;

import com.gnose.api.dto.QuoteToCreate;
import com.gnose.api.model.Quote;

public class MapQuote {

    public static Quote toEntity(QuoteToCreate quoteToCreate) {
        Quote quote = new Quote();
        quote.setQuote(quoteToCreate.getQuote());
        quote.setVotes(0);

        return quote;
    }

    public static QuoteToCreate toDto(Quote quote) {
        QuoteToCreate quoteToCreate = new QuoteToCreate();
        quoteToCreate.setQuote(quote.getQuote());
        quoteToCreate.setTimestamp(quote.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant());
        return quoteToCreate;
    }
}
