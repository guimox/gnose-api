package com.gnose.api.web;

import com.gnose.api.ai.OpenAiCorrectionService;
import com.gnose.api.ai.OpenAiModerationService;
import com.gnose.api.model.Quote;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuoteService {

  private final QuoteRepository quoteRepository;
  private final OpenAiModerationService moderationService;
  private final OpenAiCorrectionService correctionService;

  public QuoteService(
      QuoteRepository quoteRepository,
      OpenAiModerationService moderationService,
      OpenAiCorrectionService correctionService) {
    this.quoteRepository = quoteRepository;
    this.moderationService = moderationService;
    this.correctionService = correctionService;
  }

  public Quote addQuote(Quote quote) {
    String moderationResult = moderationService.moderateText(quote.getQuote());

    if (moderationResult.equals("The content is inappropriate.")) {
      throw new IllegalArgumentException(
          "The quote contains inappropriate content and cannot be added.");
    }

    String correctedQuote = correctionService.correctAndDetectValidQuote(quote.getQuote());
    String returnError = "The provided phrase is not a valid quote.";

    if (correctedQuote.equals(returnError)
        || correctedQuote.trim().isEmpty()) {
      throw new IllegalArgumentException(returnError);
    }

    quote.setQuote(correctedQuote);

    return quoteRepository.save(quote);
  }

  public List<Quote> getAllQuotes() {
    return quoteRepository.findAll();
  }

  public Optional<Quote> getQuoteById(Integer id) {
    return quoteRepository.findById(id);
  }

  public void deleteQuote(Integer id) {
    quoteRepository.deleteById(id);
  }
}
