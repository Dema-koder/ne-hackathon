package ru.demyan.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300_000) // 5 минут
                .responseTimeout(Duration.ofHours(2)) // 2 часа
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(7200, TimeUnit.SECONDS)); // 2 часа
                    conn.addHandlerLast(new WriteTimeoutHandler(7200, TimeUnit.SECONDS)); // 2 часа
                });

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                })
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}