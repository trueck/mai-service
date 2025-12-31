package org.example.config;

import org.example.tools.GameTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import java.util.List;

@Configuration
public class SpringConfig{
    @Bean
    RestClientCustomizer logbookCustomizer(
            LogbookClientHttpRequestInterceptor interceptor) {
        return restClient -> restClient.requestInterceptor(interceptor);
    }

    @Bean
    ChatClient chatClient(
            ChatClient.Builder chatClientBuilder, VectorStore vectorStore, ChatMemory chatMemory,  GameTools gameTools) {

//        return chatClientBuilder
//                .defaultAdvisors(
//                        QuestionAnswerAdvisor.builder(vectorStore).build())
//                .build();


//        var advisor = RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(
//                        VectorStoreDocumentRetriever.builder()
//                                .vectorStore(vectorStore)
//                                .build())

//                .queryExpander(
//                        MultiQueryExpander.builder()
//                                .chatClientBuilder(chatClientBuilder)
//                                .build())
//                .queryTransformers(
//                        TranslationQueryTransformer.builder()
//                                .chatClientBuilder(chatClientBuilder)
//                                .targetLanguage("English")
//                                .build(),
//                        RewriteQueryTransformer.builder()
//                                .chatClientBuilder(chatClientBuilder)
//                                .build())
//                .build();
//
//        return chatClientBuilder
//                .defaultAdvisors(
//                        PromptChatMemoryAdvisor.builder(chatMemory).build(),
//                        //MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        advisor)
//                .defaultTools(gameTools)
//                .build();
        var safeGuardAdvisor = SafeGuardAdvisor.builder()
                .sensitiveWords(List.of("Uno", "uno", "UNO"))
                .failureResponse("We don't talk about UNO. No no no... " +
                        "We don't talk about UNO. But...you can ask me about other games.")
                .build();
        return chatClientBuilder
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().build()).build(),
                        safeGuardAdvisor
                )
//                        MessageChatMemoryAdvisor.builder(
//                                MessageWindowChatMemory.builder().build()).build())
                .defaultTools(gameTools)
                .build();
    }

//    @Bean
//    ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(chatMemoryRepository)
//                .maxMessages(50)
//                .build();
//    }

    @Bean
    ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }

}
