package com.gnose.api.ai;

import org.springframework.ai.moderation.*;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.ai.openai.OpenAiModerationOptions;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class OpenAiModerationService {

  private final OpenAiModerationModel openAiModerationModel;

  public OpenAiModerationService(OpenAiModerationModel moderationModel) {
    this.openAiModerationModel = moderationModel;
  }

  private boolean isGibberish(String text) {
    Pattern gibberishPattern = Pattern.compile(".*(\\w)\\1{3,}.*|.*[^a-zA-Z0-9\\s].*|.*\\b[a-z]{1,3}\\b.*");
    return gibberishPattern.matcher(text).matches();
  }

  public String moderateText(String textToModerate) {
    if (isGibberish(textToModerate)) {
      return "The text seems to be gibberish.";
    }

    OpenAiModerationOptions moderationOptions = OpenAiModerationOptions.builder()
            .withModel("text-moderation-latest")
            .build();

    ModerationPrompt moderationPrompt = new ModerationPrompt(textToModerate, moderationOptions);

    ModerationResponse moderationResponse = openAiModerationModel.call(moderationPrompt);

    Moderation moderation = moderationResponse.getResult().getOutput();

    boolean isFlagged = moderation.getResults().stream().anyMatch(ModerationResult::isFlagged);
    return isFlagged ? "The content is inappropriate." : "The content is appropriate.";
  }
}
