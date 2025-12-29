package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.example.models.Question;
import org.example.services.GameRulesService;
import org.example.services.SpringAiBoardGameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.io.IOException;
import java.nio.charset.Charset;

@EnableWireMock(
    @ConfigureWireMock(baseUrlProperties = "openai.base.url")) 
@SpringBootTest(
    properties = "spring.ai.openai.base-url=${openai.base.url}") 
public class SpringAiBoardGameServiceWireMockTests {

  @Value("classpath:/test-ollama-response.json")
  Resource responseResource;

  @Autowired
  ChatClient chatClient;

  @MockitoBean
  GameRulesService gameRulesService;

  @MockitoBean
  VectorStore vectorStore;

  @BeforeEach
  public void setup() throws IOException {
    var cannedResponse =
        responseResource.getContentAsString(Charset.defaultCharset());
    var mapper = new ObjectMapper();
    var responseNode = mapper.readTree(cannedResponse);
    WireMock.stubFor(WireMock.post("/api/chat")
        .willReturn(ResponseDefinitionBuilder.okForJson(responseNode))); 
  }

  @Test
  public void testAskQuestion() {
    var boardGameService =
        new SpringAiBoardGameService(chatClient, gameRulesService, vectorStore);
    String conversationId = "Default";
    var answer =
        boardGameService.askQuestion(
            new Question("DouDiZhu", "What is the capital of France?"), conversationId);
    Assertions.assertThat(answer).isNotNull();
    Assertions.assertThat(answer.answer()).isEqualTo("Paris");   
  }
}
