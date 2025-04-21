package com.gnose.api.web.quote;

import com.gnose.api.dto.quote.request.QuoteRequestDTO;
import com.gnose.api.dto.quote.response.QuoteResponseDTO;
import com.gnose.api.dto.quote.request.QuoteToCreateDTO;
import com.gnose.api.model.Category;
import com.gnose.api.model.Language;
import com.gnose.api.model.Quote;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping("/correct")
    public ResponseEntity<?> correctAndStoreQuote(@RequestBody QuoteRequestDTO quoteRequestDTO) {
        try {
            QuoteToCreateDTO quoteToCreateDTO = quoteService.correctAndStoreQuote(quoteRequestDTO.getQuote());
            return ResponseEntity.ok(quoteToCreateDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchQuotes(
            @RequestParam(required = false) String quote,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer languageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Category category = categoryId != null ? new Category(categoryId) : null;
        Language language = languageId != null ? new Language(languageId) : null;

        Page<Quote> quotePage = quoteService.searchQuotes(quote, category, language, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("quotes", quotePage.getContent());
        response.put("currentPage", quotePage.getNumber());
        response.put("totalItems", quotePage.getTotalElements());
        response.put("totalPages", quotePage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/random")
    public ResponseEntity<QuoteResponseDTO> getRandomQuote() {
        QuoteResponseDTO randomQuote = quoteService.getRandomQuote();
        return ResponseEntity.ok(randomQuote);
    }

    @PostMapping
    public ResponseEntity<QuoteResponseDTO> createQuote(@RequestParam String hashId) {
        try {
            QuoteResponseDTO responseDto = quoteService.addQuoteWithHashId(hashId);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllQuotes(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Page<QuoteResponseDTO> quotePage = quoteService.getAllQuotes(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("quotes", quotePage.getContent());
        response.put("currentPage", quotePage.getNumber());
        response.put("totalItems", quotePage.getTotalElements());
        response.put("totalPages", quotePage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> getQuoteById(@PathVariable Integer id) {
        return quoteService.getQuoteById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{quoteId}/upvote")
    public ResponseEntity<?> upvoteQuote(@PathVariable int quoteId) {
        int voteUpdated = quoteService.upvoteQuote(quoteId);

        Map<String, Object> response = new HashMap<>();
        response.put("votes", voteUpdated);
        response.put("message", "Successfully upvoted");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{quoteId}/downvote")
    public ResponseEntity<?> downvoteQuote(@PathVariable int quoteId) {
        int voteUpdated = quoteService.downvoteQuote(quoteId);

        Map<String, Object> response = new HashMap<>();
        response.put("votes", voteUpdated);
        response.put("message", "Successfully downvoted");

        return ResponseEntity.ok(response);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteQuote(@PathVariable Integer id) {
//        quoteService.deleteQuote(id);
//        return ResponseEntity.noContent().build();
//    }

}
