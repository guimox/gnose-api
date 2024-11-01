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
    public ResponseEntity<String> correctQuote(@RequestBody String quoteText) {
        String correctedQuote = quoteService.correctQuote(quoteText);
        return ResponseEntity.ok(correctedQuote);
    }

    @PostMapping
    public ResponseEntity<Quote> createQuote(@Valid @RequestBody Quote quote) {
        Quote savedQuote = quoteService.addQuote(quote);
        return ResponseEntity.ok(savedQuote);
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
