package com.jtcoding.tvspainschedulecollector.configs;

import com.jtcoding.tvspainschedulecollector.services.TMDBApiClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
@AllArgsConstructor
public class TMDBConfigClient {

  @Value("${tmdb.url}")
  private final String tmdb_url;

  @Value("${tmdb.token}")
  private final String token;

  @Bean
  TMDBApiClient tmdbApiClient() {
    ReactorClientHttpConnector connector = new ReactorClientHttpConnector(HttpClient.create());
    WebClient webClient =
        WebClient.builder()
            .clientConnector(connector)
            .baseUrl(tmdb_url)
            .defaultHeader("Authorization", "Bearer "+token)
            .defaultHeader("accept", "application/json")
            .build();

    return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient))
        .build()
        .createClient(TMDBApiClient.class);
  }
}
