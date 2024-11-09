package com.gnose.api.web.quote;

import com.gnose.api.dto.quote.QuoteRequest;
import com.gnose.api.dto.quote.QuoteResponseDTO;
import com.gnose.api.dto.quote.QuoteToCreate;
import com.gnose.api.model.Quote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quotes")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @PostMapping("/correct")
    public ResponseEntity<?> correctAndStoreQuote(@RequestBody QuoteRequest quoteRequest) {
        try {
            QuoteToCreate quoteToCreate = quoteService.correctAndStoreQuote(quoteRequest.getQuote());
            return ResponseEntity.ok(quoteToCreate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
    public List<Quote> getAllQuotes() {
        return quoteService.getAllQuotes();
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
