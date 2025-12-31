package org.example.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.services.BoardGameService;
import org.example.models.Answer;
import org.example.models.Question;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AskController {

    private final BoardGameService boardGameService;

    public AskController(BoardGameService boardGameService) {
        this.boardGameService = boardGameService;
    }

    @PostMapping(path="/ask", produces="application/json")
    public Answer ask(@AuthenticationPrincipal UserDetails userDetails,
                      @RequestHeader(name="X_AI_CONVERSATION_ID",
            defaultValue = "default") String conversationId,
                      @RequestBody @Valid Question question) {
        log.info("receiving question: {}", question.question());
        return boardGameService.askQuestion(question, userDetails.getUsername() + "_" + conversationId);
    }

}