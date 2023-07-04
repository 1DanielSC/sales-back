package com.salesback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//COMMENT: ZIPKIN
//import brave.sampler.Sampler;
// @EnableCaching
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class SalesBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesBackApplication.class, args);
	}
/* 
	@Bean
	public Sampler defaultSampler(){
		return Sampler.ALWAYS_SAMPLE;
	}
*/
	@LoadBalanced
	@Bean
	public RestTemplate getRestTemplate(){
		return new RestTemplate();
	}
}
