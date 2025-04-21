package com.gnose.api.ai;

import com.gnose.api.dto.ai.ChatRequestDTO;
import com.gnose.api.dto.ai.ChatResponse;
import com.gnose.api.dto.quote.response.QuoteResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class CorrectionService {

    @Autowired
    @Qualifier("openaiRestTemplate")
    private RestTemplate restTemplate;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    public QuoteResponseDTO correctAndDetectValidQuote(String phraseToCheck) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("prompt", "Correct the following text: " + phraseToCheck);
        requestBody.put("max_tokens", 60);
        requestBody.put("temperature", 0.5);

        try {
            ChatRequestDTO request = new ChatRequestDTO(
                    model,
                    "You are an expert in identifying and categorizing meaningful and impactful quotes. " +
                            "Correct any spelling, grammar, or word usage mistakes in the input. Always remove ? or " +
                            "!, putting the final period in the quote. " +
                            "Identify the language of the input. " +
                            "If the input is not a valid quote, return 'This is not a valid quote.' " +
                            "If it is valid, categorize the quote as 'Motivational,' 'Humorous,' 'Philosophical,' or 'Other,' and " +
                            "provide both the corrected quote, language used, and the category label. Correct this: " + phraseToCheck
            );

            String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
            ChatResponse response = restTemplate.postForObject(OPENAI_API_URL, request, ChatResponse.class);
            assert response != null;

            String correctedText = response.getChoices().get(0).getMessage().getContent();

            if (correctedText.toLowerCase().contains("this is not a valid quote")) {
                throw new IllegalArgumentException("The provided phrase is not a valid quote.");
            } else {
                return parseQuoteResponse(correctedText);
            }
        } catch (HttpClientErrorException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Request Body: " + requestBody.toString());
        }
        return null;
    }

    private QuoteResponseDTO parseQuoteResponse(String responseText) {
        String correctedQuote = null;
        String categoryLabel = null;
        String language = null;

        String[] lines = responseText.split("\n");
        for (String line : lines) {
            if (line.startsWith("Corrected Quote:")) {
                correctedQuote = line.replace("Corrected Quote:", "").trim().replaceAll("^\"|\"$", "");
            } else if (line.startsWith("Language:")) {
                language = line.replace("Language:", "").trim();
            } else if (line.startsWith("Category:")) {
                categoryLabel = line.replace("Category:", "").trim();
            }
        }

        return new QuoteResponseDTO(correctedQuote, categoryLabel, language);
    }
}
