package com.gnose.api.web;

import com.gnose.api.ai.OpenAiCorrectionService;
import com.gnose.api.ai.OpenAiModerationService;
import com.gnose.api.model.Quote;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final OpenAiModerationService moderationService;
    private final OpenAiCorrectionService correctionService;

    public QuoteService(QuoteRepository quoteRepository, OpenAiModerationService moderationService, OpenAiCorrectionService correctionService) {
        this.quoteRepository = quoteRepository;
        this.moderationService = moderationService;
        this.correctionService = correctionService;
    }

    public String correctQuote(String quoteText) {
        return correctionService.correctAndDetectValidQuote(quoteText);
    }

    public Quote addQuote(Quote quote) {
        String moderationResult = moderationService.moderateText(quote.getQuote());

        if (moderationResult.equals("The content is inappropriate.")) {
            throw new IllegalArgumentException("The quote contains inappropriate content and cannot be added.");
        }

        quote.setQuote(moderationResult);

        return quoteRepository.save(quote);
    }


    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    public Optional<Quote> getQuoteById(Integer id) {
        return quoteRepository.findById(id);
    }

    public void upvoteQuote(int quoteId) {
        quoteRepository.incrementVotesByOne(quoteId);
    }

    public void downvoteQuote(int quoteId) {
        quoteRepository.decrementVotesByOne(quoteId);
    }

//    public void deleteQuote(Integer id) {
//        quoteRepository.deleteById(id);
//    }
}
