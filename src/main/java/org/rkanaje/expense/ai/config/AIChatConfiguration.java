package org.rkanaje.expense.ai.config;

import org.eclipse.jetty.client.HttpClient;
import org.rkanaje.expense.ai.service.RestClientInterceptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class AIChatConfiguration {

    @Bean
    public OllamaApi ollamaApi(OllamaConfig ollamaConfig) {
        RestClient.Builder restClientBuilder = RestClient.builder();
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings
                .defaults()
                .withConnectTimeout(Duration.ofSeconds(60))
                .withReadTimeout(Duration.ofSeconds(300));

        HttpClient httpClient = new HttpClient();
        httpClient.setIdleTimeout(60000);
        httpClient.setConnectTimeout(60000);

        JettyClientHttpConnector connector = new JettyClientHttpConnector(httpClient);
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.httpComponents().build(settings);
        restClientBuilder.requestFactory(requestFactory)
                .requestInterceptor(new RestClientInterceptor());
        WebClient.Builder webClientBuilder = WebClient.builder().clientConnector(connector);
        return OllamaApi.builder()
                .webClientBuilder(webClientBuilder)
                .restClientBuilder(restClientBuilder)
                .baseUrl(ollamaConfig.getHost())
                .build();
        // return new OllamaApi(ollamaHost, builder, webClientBuilder);
    }

    @Bean
    public ChatModel chatModel(OllamaApi ollamaApi, OllamaConfig ollamaConfig) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaOptions.builder()
                                .model(ollamaConfig.getChat().getModel())
                                .numCtx(ollamaConfig.getChat().getOptions().getNumCtx())
                                .temperature(ollamaConfig.getChat().getOptions().getTemperature())
                                .build())
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

}
