package org.example.services;


import lombok.extern.slf4j.Slf4j;
import org.example.models.Answer;
import org.example.models.Question;
import org.example.tools.GameTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

//import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.FILTER_EXPRESSION;
import java.util.Collection;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;
import static org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever.FILTER_EXPRESSION;



@Service
@Slf4j
public class SpringAiBoardGameService implements BoardGameService {

    private final ChatClient chatClient;
    private final GameRulesService gameRulesService;
    private final VectorStore vectorStore;
    private final GameTools gameTools;
    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    Resource promptTemplate;


    public SpringAiBoardGameService(ChatClient chatClient,GameRulesService gameRulesService,
                                    VectorStore vectorStore,
                                    GameTools gameTools) {
        this.chatClient = chatClient;
        this.gameRulesService = gameRulesService;
        this.vectorStore = vectorStore;
        this.gameTools = gameTools;
    }

    @Override
    public Answer askQuestion(Question question, String conversationId) {
      //  var gameRules = gameRulesService.getRulesFor(question.gameTitle(), question.question());

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

//        var responseEntity = chatClient.prompt()
//                .system(systemSpec -> systemSpec
//                        .text(promptTemplate)
//                        .param("gameTitle", question.gameTitle())
//                        .param("rules", gameRules))
//                .user(question.question())
//                .call()
//              //  .entity(Answer.class);
//                .responseEntity(Answer.class);
//        var response = responseEntity.response();
//
//        var metadata = response.getMetadata();
//        logUsage(metadata.getUsage());
//
//
//        return responseEntity.entity();
      // return answer;
        var gameNameMatch = String.format(
                "gameTitle == '%s'",
                normalizeGameTitle(question.gameTitle()));


        var response = chatClient.prompt()
                .system(systemSpec -> systemSpec
                        .text(promptTemplate)
                        .param("gameTitle", question.gameTitle()))
                .user(question.question())
                .advisors(advisorSpec ->
                        advisorSpec.param(FILTER_EXPRESSION,  getDocumentMatchExpression(question.gameTitle())))
//                .advisors(advisorSpec ->
//                        advisorSpec.param(FILTER_EXPRESSION, gameNameMatch)
//                                .param(CONVERSATION_ID, conversationId))
              //  .tools(gameTools)
                .call()
                .content();
        return new Answer(question.gameTitle(), response);

    }

    private String normalizeGameTitle(String gameTitle) {
        return gameTitle.toLowerCase().replace(" ", "_");
    }

    private void logUsage(Usage usage) {
        log.info("Token usage: prompt={}, generation={}, total={}",
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens());
    }

    private String getDocumentMatchExpression(String gameTitle) {
        return String.format("gameTitle == '%s' %s",
                normalizeGameTitle(gameTitle),
                getPremiumContentFilterExpression());
    }

    private static String getPremiumContentFilterExpression() {
        Collection<? extends GrantedAuthority> authorities =
                SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities();

        if (authorities.stream().noneMatch(
                a -> a.getAuthority().equals("ROLE_PREMIUM_USER"))) {
            return "AND documentType != 'PREMIUM'";
        }

        return "";
    }
}
