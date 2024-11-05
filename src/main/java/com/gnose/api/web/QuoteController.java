package com.gnose.api.web;

import com.gnose.api.model.Quote;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quotes")
public class QuoteController {

    @Autowired
    private QuoteService quoteService;

    @PostMapping("/correct")
    public ResponseEntity<String> correctAndStoreQuote(@RequestBody String quoteText) {
        try {
            String hashId = quoteService.correctAndStoreQuote(quoteText);
            return ResponseEntity.ok(hashId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Quote> createQuote(@RequestParam String hashId) {
        try {
            Quote savedQuote = quoteService.addQuoteWithHashId(hashId);
            return ResponseEntity.ok(savedQuote);
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
    public ResponseEntity<String> upvoteQuote(@PathVariable int quoteId) {
        quoteService.upvoteQuote(quoteId);
        return new ResponseEntity<>("Quote upvoted successfully", HttpStatus.OK);
    }

    @PostMapping("/{quoteId}/downvote")
    public ResponseEntity<String> downvoteQuote(@PathVariable int quoteId) {
        quoteService.downvoteQuote(quoteId);
        return new ResponseEntity<>("Quote downvoted successfully", HttpStatus.OK);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteQuote(@PathVariable Integer id) {
//        quoteService.deleteQuote(id);
//        return ResponseEntity.noContent().build();
//    }

}
