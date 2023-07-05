package com.salesback.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.salesback.model.Item;
import com.salesback.model.dto.ProductDTO;
import com.salesback.service.interfaces.ProductServiceClient;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class OrderResilience {
    
    @Autowired
    private ProductServiceClient productClient;

    @CircuitBreaker(name = "productservice", fallbackMethod = "requestProductFallBack")
    @RateLimiter(name = "rate_productservice", fallbackMethod = "requestProductRateLimiterFallBack")
    @Bulkhead(name = "bulk_productservice", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "requestProductBulkheadFallBack")
    @Retry(name = "retry_productservice", fallbackMethod = "requestProductRetryFallBack")
    public ResponseEntity<ProductDTO> requestProduct(ProductDTO product){
        return productClient.requestProduct(product);
    }

    public ResponseEntity<ProductDTO> requestProductFallBack(Throwable throwable){
        System.out.println("Circuit breaker (requestProduct): " + throwable.getMessage());
        System.out.println("Cause: " + throwable.getCause());
        return ResponseEntity.internalServerError().build();
    }

    public ResponseEntity<ProductDTO> requestProductBulkheadFallBack(Throwable throwable){
        System.out.println("Bulkhead (requestProduct): " + throwable.getMessage());
        System.out.println("Cause: " + throwable.getCause());
        return ResponseEntity.internalServerError().build();
    }

    public ResponseEntity<ProductDTO> requestProductRetryFallBack(Throwable throwable){
        System.out.println("Retry (requestProduct): " + throwable.getMessage());
        System.out.println("Cause: " + throwable.getCause());
        return ResponseEntity.internalServerError().build();
    }

    public ResponseEntity<ProductDTO> requestProductRateLimiterFallBack(Throwable throwable){
        System.out.println("Rate Limiter (requestProduct): " + throwable.getMessage());
        System.out.println("Cause: " + throwable.getCause());
        return ResponseEntity.internalServerError().build();
    }

    @CircuitBreaker(name = "productservice", fallbackMethod = "increaseQuantityBulkheadFallBack")
    @Bulkhead(name = "bulk_productservice", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "requestProductFallBack")
    public ResponseEntity<List<ProductDTO>> increaseQuantity(List<Item> items){
        return productClient.increaseQuantity(items);
    }

    public ResponseEntity<ProductDTO> increaseQuantityFallBack(Throwable throwable){
        System.out.println("Circuit breaker (increaseQuantity): " + throwable.getMessage());
        return ResponseEntity.internalServerError().build();
    }

    public ResponseEntity<ProductDTO> increaseQuantityBulkheadFallBack(Throwable throwable){
        System.out.println("Bulkhead (increaseQuantity): " + throwable.getMessage());
        return ResponseEntity.internalServerError().build();
    }

}
