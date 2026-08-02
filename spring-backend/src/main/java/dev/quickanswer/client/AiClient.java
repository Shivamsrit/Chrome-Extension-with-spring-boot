package dev.quickanswer.client;

import dev.quickanswer.config.AiProperties;
import dev.quickanswer.dto.AiReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static java.util.logging.Level.parse;

@Component
public class AiClient {
    private static final Logger log = LoggerFactory.getLogger(AiClient.class);
    private final RestClient restClient;
    private final AiProperties props;
    public AiClient(RestClient restClient, AiProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public AiReply askWithImage(String systemPrompt, String userPrompt, String imageDataUrl) {
        Map<String, Object> imagePart = Map.of(
                "type", "image_url",
                "image_url", Map.of("url", imageDataUrl, "detail", "high"));

        Map<String, Object> textPart = Map.of("type", "text", "text", userPrompt);

        Map<String, Object> payload = Map.of(
                "model", props.model(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", List.of(textPart, imagePart))),
                "max_tokens", 700,
                "temperature", 0);

        try {
            log.info("Sending request to AI provider with model: {}", props.model());
            Map<?, ?> response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + props.apiKey())
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            return parse(response);
        } catch (AiClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI request failed", e);
            throw new AiClientException("AI request failed: " + e.getMessage(), e);
        }
    }
    private AiReply parse(Map<?, ?> response) {
        if (response == null) {
            throw new AiClientException("Empty AI response");
        }
        List<?> choices = (List<?>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new AiReply(null, null);
        }
        Map<?, ?> choice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = choice != null ? (Map<?, ?>) choice.get("message") : null;
        if (message == null) {
            return new AiReply(null, null);
        }
        Object content = message.get("content");
        Object refusal = message.get("refusal");
        return new AiReply(
                content != null ? String.valueOf(content) : null,
                refusal != null ? String.valueOf(refusal) : null);
    }

}
