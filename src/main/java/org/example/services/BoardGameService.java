package org.example.services;

import org.example.models.Answer;
import org.example.models.Question;

public interface BoardGameService {
    Answer askQuestion(Question question, String conversationId);
}