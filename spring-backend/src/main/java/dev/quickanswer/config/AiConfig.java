package dev.quickanswer.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    public RestClient restClient(AiProperties props) {
        return RestClient.builder().baseUrl(props.baseUrl()).build();
    }
}
