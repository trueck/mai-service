package org.example;

import org.assertj.core.api.Assertions;
import org.example.models.Question;
import org.example.services.SpringAiBoardGameService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.stringtemplate.v4.ST;

import java.nio.charset.Charset;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(SpringAiBoardGameService.class)
public class SpringAiBoardGameServiceMockOpenAiTests {

    @Autowired
    MockRestServiceServer mockServer;

    @Autowired
    SpringAiBoardGameService service;

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public ChatClient.Builder chatClientBuilder(RestClient.Builder restClientBuilder, WebClient.Builder webClientBuilder) {
            var openAiChatModel = OllamaChatModel.builder()
                    .ollamaApi(
                        OllamaApi.builder()
                            .baseUrl("http://localhost:11434")
                            //.apiKey("TEST_API_KEY")
                            .restClientBuilder(restClientBuilder)
                            .webClientBuilder(webClientBuilder)
                            .build())
                    .build();
            return ChatClient.builder(openAiChatModel);
        }
    }

    @Test
    public void testStuff() throws Exception {
        var expectedAnswer = "Checkers is a game for two players.";
        mockOpenAiChatResponse(expectedAnswer);
        String conversationId = "Default";
        var answer = service.askQuestion(new Question("DouDiZhu", "How many can play checkers?"), conversationId);
        Assertions.assertThat(answer.answer()).isEqualTo(expectedAnswer);
    }

    private void mockOpenAiChatResponse(String content) throws Exception {
        var responseResource = new ClassPathResource("/response.json");
        var st = new ST(StreamUtils.copyToString(responseResource.getInputStream(), Charset.defaultCharset()), '$', '$');
        st = st.add("content", content);

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
            .andRespond(withSuccess(st.render(), MediaType.APPLICATION_JSON));
    }

}