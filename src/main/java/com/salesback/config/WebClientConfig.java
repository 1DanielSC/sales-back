package com.salesback.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.salesback.service.interfaces.ProductServiceClient;

@Configuration
public class WebClientConfig {
    @Autowired
    private ReactorLoadBalancerExchangeFilterFunction lbFunction;

    @Bean
    public WebClient loadBalancedWebClientBuilder(){
        return WebClient.builder()
        .baseUrl("http://PRODUCT")
        .filter(lbFunction)
        .build();
    }

    @Bean
    public ProductServiceClient productClient(WebClient webClient){
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(webClient))
            .build();

        return factory.createClient(ProductServiceClient.class);
    }
}
