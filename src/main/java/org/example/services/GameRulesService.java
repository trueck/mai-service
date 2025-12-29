package org.example.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

@Service
public class GameRulesService {

    private static final Logger LOG =
            LoggerFactory.getLogger(GameRulesService.class);

    private final VectorStore vectorStore;

    public GameRulesService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String getRulesFor(String gameName, String question) {

        var searchRequest = SearchRequest
                .builder()
                .query(question)
                .filterExpression(
                        new FilterExpressionBuilder()
                                .eq("gameTitle", normalizeGameTitle(gameName)).build())
                .build();

        System.err.println("Search request: " + searchRequest);

        var similarDocs =
                vectorStore.similaritySearch(searchRequest);

        if (similarDocs.isEmpty()) {
            return "The rules for " + gameName + " are not available.";
        }

        return similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));
//        try {
//            var filename = String.format(
//                    "classpath:/gameRules/%s.txt",
//                    gameName.toLowerCase().replace(" ", "_"));
//
//            return new DefaultResourceLoader()
//                    .getResource(filename)
//                    .getContentAsString(Charset.defaultCharset());
//        } catch (IOException e) {
//            LOG.info("No rules found for game: " + gameName);
//            return "";
//        }
    }

    private String normalizeGameTitle(String gameTitle) {
        return gameTitle.toLowerCase().replace(" ", "_");
    }
}