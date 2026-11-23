package py.edu.uc.lp3.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient minecraftWebClient(WebClient.Builder builder,
            @Value("${minecraft-api.base-url:http://10.40.142.72:8081}") String baseUrl,
            @Value("${minecraft-api.connect-timeout:3s}") Duration connectTimeout) {
        HttpClient httpClient = HttpClient.create()
                .disableRetry(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()));
        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient objetosWebClient(WebClient.Builder builder,
            @Value("${restful-api.base-url:https://api.restful-api.dev}") String baseUrl,
            @Value("${restful-api.connect-timeout:3s}") Duration connectTimeout) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectTimeout.toMillis()));
        return builder.baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
