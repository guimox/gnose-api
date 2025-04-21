package com.gnose.api.web.quote;

import com.gnose.api.ai.CorrectionService;
import com.gnose.api.ai.ModerationService;
import com.gnose.api.dto.quote.response.QuoteResponseDTO;
import com.gnose.api.dto.quote.request.QuoteToCreateDTO;
import com.gnose.api.mapper.MapperQuote;
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
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ModerationService moderationService;
    private final CorrectionService correctionService;
    private final CategoryRepository categoryRepository;
    private final LanguageRepository languageRepository;
    private final Map<String, QuoteToCreateDTO> temporaryQuotes = new ConcurrentHashMap<>();

    private static final long EXPIRY_DURATION_MS = 10 * 60 * 1000; // 10 minutes

    @Value("${spring.app.secretKey}")
    private String secretKey;

    public QuoteService(QuoteRepository quoteRepository, ModerationService moderationService,
                        CorrectionService correctionService, CategoryRepository categoryRepository,
                        LanguageRepository languageRepository) {
        this.quoteRepository = quoteRepository;
        this.moderationService = moderationService;
        this.correctionService = correctionService;
        this.categoryRepository = categoryRepository;
        this.languageRepository = languageRepository;
    }

    public Page<Quote> searchQuotes(String quote, Category category, Language language, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return quoteRepository.searchQuotes(quote, category, language, pageable);
    }

    public QuoteToCreateDTO correctAndStoreQuote(String quoteText) throws NoSuchAlgorithmException {
        QuoteResponseDTO quoteResponse = correctionService.correctAndDetectValidQuote(quoteText);
        if (quoteResponse == null || quoteResponse.getQuote() == null) {
            throw new IllegalArgumentException("Quote correction failed or returned null.");
        }

        String moderationResult = moderationService.moderateText(quoteResponse.getQuote());
        if (moderationResult == null) {
            throw new IllegalArgumentException("Moderation service returned no result.");
        }

        if ("The content is inappropriate.".equals(moderationResult)) {
            throw new IllegalArgumentException("The quote contains inappropriate content and cannot be added.");
        }

        Instant timestamp = Instant.now();
        String hashId = generateHashId(quoteResponse.getQuote(), timestamp);

        QuoteToCreateDTO quoteToCreateDTO = new QuoteToCreateDTO(
                quoteResponse.getQuote(),
                hashId,
                timestamp,
                quoteResponse.getLanguage().getName(),
                quoteResponse.getCategory().getName()
        );

        temporaryQuotes.put(hashId, quoteToCreateDTO);
        return quoteToCreateDTO;
    }

    public QuoteResponseDTO getRandomQuote() {
        long totalQuotes = quoteRepository.count();
        int randomIndex = new Random().nextInt((int) totalQuotes);
        Pageable pageable = PageRequest.of(randomIndex, 1);
        Page<Quote> randomQuotePage = quoteRepository.findAll(pageable);
        return MapperQuote.toResponseDto(randomQuotePage.getContent().get(0));
    }

    public QuoteResponseDTO addQuoteWithHashId(String hashId) {
        QuoteToCreateDTO quoteToCreateDTO = temporaryQuotes.remove(hashId);
        if (quoteToCreateDTO == null) {
            throw new IllegalArgumentException("Invalid or expired quote hash.");
        }

        Language language = languageRepository.findByName(quoteToCreateDTO.getLanguage())
                .orElseGet(() -> languageRepository.save(new Language(quoteToCreateDTO.getLanguage())));

        Category category = categoryRepository.findByName(quoteToCreateDTO.getCategory())
                .orElseGet(() -> categoryRepository.save(new Category(quoteToCreateDTO.getCategory())));

        Optional<Quote> existingQuote = quoteRepository.findByQuote(
                quoteToCreateDTO.getQuote());

        if (existingQuote.isPresent()) {
            return MapperQuote.toResponseDto(existingQuote.get());
        }

        Quote quote = MapperQuote.toEntity(quoteToCreateDTO, language, category);
        Quote savedQuote = quoteRepository.save(quote);
        return MapperQuote.toResponseDto(savedQuote);
    }

    public Page<QuoteResponseDTO> getAllQuotes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Quote> quotesPage = quoteRepository.findAllByOrderByIdDesc(pageable);

        return quotesPage.map(quote -> MapperQuote.toResponseDto(quote));
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
