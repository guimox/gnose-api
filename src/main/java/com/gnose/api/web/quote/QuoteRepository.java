package com.gnose.api.web.quote;

import com.gnose.api.model.Category;
import com.gnose.api.model.Language;
import com.gnose.api.model.Quote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Integer> {

    Page<Quote> findAllByOrderByIdDesc(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Quote q SET q.votes = q.votes + 1 WHERE q.id = :quoteId")
    void incrementVotesByOne(int quoteId);

    @Query("SELECT q FROM Quote q " +
            "WHERE (:quote IS NULL OR LOWER(q.quote) LIKE LOWER(CONCAT('%', :quote, '%'))) " +
            "AND (:category IS NULL OR q.category = :category) " +
            "AND (:language IS NULL OR q.language = :language)")
    Page<Quote> searchQuotes(String quote, Category category, Language language, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Quote q SET q.votes = q.votes - 1 WHERE q.id = :quoteId")
    void decrementVotesByOne(int quoteId);

    @Query("SELECT q.votes FROM Quote q WHERE q.id = :quoteId")
    int findVotesByQuoteId(int quoteId);

    Optional<Quote> findByQuoteAndLanguageAndCategory(String quote, Language language, Category category);

    Optional<Quote> findByQuote(String quote);
}
