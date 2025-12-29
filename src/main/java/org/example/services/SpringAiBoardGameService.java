package org.example.services;


import lombok.extern.slf4j.Slf4j;
import org.example.models.Answer;
import org.example.models.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpringAiBoardGameService implements BoardGameService {

    private final ChatClient chatClient;
    private final GameRulesService gameRulesService;
    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource promptTemplate;


    public SpringAiBoardGameService(ChatClient.Builder chatClientBuilder,GameRulesService gameRulesService) {
        this.chatClient = chatClientBuilder.build();
        this.gameRulesService = gameRulesService;
    }

    @Override
    public Answer askQuestion(Question question) {
        var gameRules = gameRulesService.getRulesFor(question.gameTitle(), question.question());

//        var answerText = chatClient.prompt()
//                .system(systemSpec -> systemSpec
//                        .text(promptTemplate)
//                        .param("gameTitle", question.gameTitle())
//                        .param("rules", gameRules))
//                .user(question.question())
//                .call()
//                .content();
//        log.info("the answer is : {}", answerText);
//        return new Answer(question.gameTitle(), answerText);

        var responseEntity = chatClient.prompt()
                .system(systemSpec -> systemSpec
                        .text(promptTemplate)
                        .param("gameTitle", question.gameTitle())
                        .param("rules", gameRules))
                .user(question.question())
                .call()
              //  .entity(Answer.class);
                .responseEntity(Answer.class);
        var response = responseEntity.response();

        var metadata = response.getMetadata();
        logUsage(metadata.getUsage());


        return responseEntity.entity();
      // return answer;
    }

    private void logUsage(Usage usage) {
        log.info("Token usage: prompt={}, generation={}, total={}",
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());
    }
}
