package org.example.exceptions;

public class ModerationException extends RuntimeException {
    public ModerationException(String category) {
        super(String.format(
                "Moderation failed. Content identified as %s.", category));
    }
}