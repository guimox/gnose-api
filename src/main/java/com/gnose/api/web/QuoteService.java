package com.gnose.api.web;

import com.gnose.api.ai.OpenAiCorrectionService;
import com.gnose.api.ai.OpenAiModerationService;
import com.gnose.api.model.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final OpenAiModerationService moderationService;
    private final OpenAiCorrectionService correctionService;
    private final Map<String, Quote> temporaryQuotes = new ConcurrentHashMap<>();
    private static final long EXPIRY_DURATION_MS = 10 * 60 * 1000; // 10 minutes

    @Value("${spring.app.secretKey}")
    private String secretKey;

    public QuoteService(QuoteRepository quoteRepository, OpenAiModerationService moderationService, OpenAiCorrectionService correctionService) {
        this.quoteRepository = quoteRepository;
        this.moderationService = moderationService;
        this.correctionService = correctionService;
    }

    public String correctAndStoreQuote(String quoteText) throws NoSuchAlgorithmException {
        String correctedQuote = correctionService.correctAndDetectValidQuote(quoteText);
        String moderationResult = moderationService.moderateText(correctedQuote);

        if (moderationResult.equals("The content is inappropriate.")) {
            throw new IllegalArgumentException("The quote contains inappropriate content and cannot be added.");
        }

        Instant timestamp = Instant.now();
        String hashId = generateHashId(correctedQuote, timestamp);
        Quote quote = new Quote();
        quote.setQuote(correctedQuote);
        quote.setTimestamp(timestamp);
        temporaryQuotes.put(hashId, quote);

        return hashId;
    }

    public Quote addQuoteWithHashId(String hashId) {
        Quote quote = temporaryQuotes.remove(hashId);
        if (quote == null) {
            throw new IllegalArgumentException("Invalid or expired quote hash.");
        }
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

    private String generateHashId(String quoteText, Instant timestamp) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        String input = secretKey + quoteText + timestamp.toString();
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // Run every 5 minutes
    public void cleanUpExpiredQuotes() {
        Instant now = Instant.now();
        temporaryQuotes.entrySet().removeIf(entry -> now.toEpochMilli() - entry.getValue().getTimestamp().toEpochMilli() > EXPIRY_DURATION_MS);
    }
}
