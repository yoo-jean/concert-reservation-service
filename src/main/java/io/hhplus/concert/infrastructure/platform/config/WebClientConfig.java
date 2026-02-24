package io.hhplus.concert.infrastructure.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient dataPlatformWebClient() {
        return WebClient.builder().build();
    }
}