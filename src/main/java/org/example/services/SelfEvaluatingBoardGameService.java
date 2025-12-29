
package org.example.services;

import lombok.extern.slf4j.Slf4j;
import org.example.exceptions.AnswerNotRelevantException;
import org.example.models.Answer;
import org.example.models.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;


//@Service
@Slf4j
public class SelfEvaluatingBoardGameService implements BoardGameService {

  private final ChatClient chatClient;
  private final RelevancyEvaluator evaluator;

  public SelfEvaluatingBoardGameService(ChatClient.Builder chatClientBuilder) {
    var chatOptions = ChatOptions.builder()
        .model("gemma3:4b")
        .build();

    this.chatClient = chatClientBuilder
        .defaultOptions(chatOptions)
        .build();

    this.evaluator = new RelevancyEvaluator(chatClientBuilder); 
  }

  @Override
  @Retryable(retryFor = AnswerNotRelevantException.class)
  public Answer askQuestion(Question question, String conversationId) {
    var answerText = chatClient.prompt()
        .user(question.question())
        .call()
        .content();

    log.info("evaluating the answer: {}", answerText);
    evaluateRelevancy(question, answerText);

    return new Answer(question.gameTitle(), answerText);
  }

  @Recover 
  public Answer recover(AnswerNotRelevantException e) {
    return new Answer("", "I'm sorry, I wasn't able to answer the question.");
  }

  private void evaluateRelevancy(Question question, String answerText) {
    var evaluationRequest =
        new EvaluationRequest(question.question(), answerText);
    var evaluationResponse = evaluator.evaluate(evaluationRequest);
    if (!evaluationResponse.isPass()) {
      throw new AnswerNotRelevantException(question.question(), answerText); 
    }
  }

}

