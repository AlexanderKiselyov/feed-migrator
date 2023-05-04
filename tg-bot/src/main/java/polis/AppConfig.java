package polis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import polis.ok.api.OKClient;
import polis.ok.api.OkAuthorizator;
import polis.ok.api.OkClientImpl;
import polis.vk.api.VkClient;
import polis.vk.api.VkClientImpl;

import java.net.http.HttpClient;

@Configuration
public class AppConfig {
    private static final int CLIENT_RESPONSE_TIMEOUT_SECONDS = 5;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public CloseableHttpClient apacheHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(CLIENT_RESPONSE_TIMEOUT_SECONDS * 1000)
                .setConnectionRequestTimeout(CLIENT_RESPONSE_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(CLIENT_RESPONSE_TIMEOUT_SECONDS * 1000).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public OkAuthorizator okAuthorizator(@Autowired HttpClient httpClient) {
        return new OkAuthorizator(httpClient);
    }

    @Bean
    public OKClient okClient(
            @Autowired CloseableHttpClient apacheHttpClient,
            @Autowired HttpClient httpClient
    ) {
        return new OkClientImpl(apacheHttpClient, httpClient);
    }

    @Bean
    public VkClient vkClient() {
        return new VkClientImpl();
    }
}
