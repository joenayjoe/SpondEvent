package com.junaid.spond.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.junaid.spond.models.Event;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder
        .modulesToInstall(JavaTimeModule.class)
        .timeZone("UTC")
        .simpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .build();
  }

  @Bean
  public Cache<String, Event> forecastCache() {
    return Caffeine.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.HOURS).build();
  }
}
