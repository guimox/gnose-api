package com.gnose.api.web.quote;

import com.gnose.api.ai.OpenAiCorrectionService;
import com.gnose.api.ai.OpenAiModerationService;
import com.gnose.api.dto.quote.QuoteResponse;
import com.gnose.api.dto.quote.QuoteResponseDTO;
import com.gnose.api.dto.quote.QuoteToCreate;
import com.gnose.api.mapper.MapQuote;
import com.gnose.api.model.Category;
import com.gnose.api.model.Language;
import com.gnose.api.model.Quote;
import com.gnose.api.web.category.CategoryRepository;
import com.gnose.api.web.language.LanguageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final OpenAiModerationService moderationService;
    private final OpenAiCorrectionService correctionService;
    private final CategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;
    private final Map<String, QuoteToCreate> temporaryQuotes = new ConcurrentHashMap<>();
    private static final long EXPIRY_DURATION_MS = 10 * 60 * 1000; // 10 minutes

    @Value("${spring.app.secretKey}")
    private String secretKey;

    public QuoteService(QuoteRepository quoteRepository, OpenAiModerationService moderationService,
                        OpenAiCorrectionService correctionService, CategoryRepository categoryRepository, LanguageRepository languageRepository) {
        this.quoteRepository = quoteRepository;
        this.moderationService = moderationService;
        this.correctionService = correctionService;
        this.categoryRepository = categoryRepository;
        this.languageRepository = languageRepository;
    }

    public QuoteToCreate correctAndStoreQuote(String quoteText) throws NoSuchAlgorithmException {
        QuoteResponse quoteResponse = correctionService.correctAndDetectValidQuote(quoteText);
        if (quoteResponse == null || quoteResponse.getCorrectedQuote() == null) {
            throw new IllegalArgumentException("Quote correction failed or returned null.");
        }

        String moderationResult = moderationService.moderateText(quoteResponse.getCorrectedQuote());
        if (moderationResult == null) {
            throw new IllegalArgumentException("Moderation service returned no result.");
        }

        if ("The content is inappropriate.".equals(moderationResult)) {
            throw new IllegalArgumentException("The quote contains inappropriate content and cannot be added.");
        }

        Instant timestamp = Instant.now();
        String hashId = generateHashId(quoteResponse.getCorrectedQuote(), timestamp);

        QuoteToCreate quoteToCreate = new QuoteToCreate(
                quoteResponse.getCorrectedQuote(),
                hashId,
                timestamp,
                quoteResponse.getLanguage(),
                quoteResponse.getCategory()
        );

        temporaryQuotes.put(hashId, quoteToCreate);
        return quoteToCreate;
    }

    public QuoteResponseDTO addQuoteWithHashId(String hashId) {
        QuoteToCreate quoteToCreate = temporaryQuotes.remove(hashId);
        if (quoteToCreate == null) {
            throw new IllegalArgumentException("Invalid or expired quote hash.");
        }

        Language language = languageRepository.findByName(quoteToCreate.getLanguage())
                .orElseGet(() -> languageRepository.save(new Language(quoteToCreate.getLanguage())));

        Category category = categoryRepository.findByName(quoteToCreate.getCategory())
                .orElseGet(() -> categoryRepository.save(new Category(quoteToCreate.getCategory())));

        Quote quote = MapQuote.toEntity(quoteToCreate, language, category);

        Quote savedQuote = quoteRepository.save(quote);
        return MapQuote.toResponseDto(savedQuote);
    }

    public Page<Quote> getAllQuotes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return quoteRepository.findAllByOrderByIdDesc(pageable);
    }


    public Optional<Quote> getQuoteById(Integer id) {
        return quoteRepository.findById(id);
    }

    public int upvoteQuote(int quoteId) {
        quoteRepository.incrementVotesByOne(quoteId);
        return quoteRepository.findVotesByQuoteId(quoteId);
    }

    public int downvoteQuote(int quoteId) {
        quoteRepository.decrementVotesByOne(quoteId);
        return quoteRepository.findVotesByQuoteId(quoteId);
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
