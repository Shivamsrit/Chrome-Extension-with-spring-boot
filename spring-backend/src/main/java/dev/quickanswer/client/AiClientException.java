package dev.quickanswer.client;

public class AiClientException extends RuntimeException {
    public AiClientException(String message, Throwable cause) {
        super(message, cause);
    }
    public AiClientException(String message) {
        super(message);
    }
}
