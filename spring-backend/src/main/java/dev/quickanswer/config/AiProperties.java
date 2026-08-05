package dev.quickanswer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public record AiProperties(String baseUrl, String apiKey, String model) {
}
