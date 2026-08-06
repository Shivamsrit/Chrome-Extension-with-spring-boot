package dev.quickanswer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    // Supports comma-separated list, e.g.:
    //   CORS_ALLOWED_ORIGIN=chrome-extension://*,https://my-frontend.com
    // Default allows all Chrome extensions (needed for extension service-worker fetch)
    @Value("${app.cors.allowed-origin:chrome-extension://*}")
    private String allowedOrigin;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // allowedOriginPatterns supports wildcards; allowedOrigins does NOT
                String[] patterns = allowedOrigin.split(",");
                registry.addMapping("/**")
                        .allowedOriginPatterns(patterns)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false); // must be false when using wildcard patterns
            }
        };
    }
}