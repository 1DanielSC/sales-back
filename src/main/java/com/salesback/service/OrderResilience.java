package com.salesback.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.salesback.model.Item;
import com.salesback.model.dto.ProductDTO;
import com.salesback.service.interfaces.ProductServiceClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class OrderResilience {
    
    @Autowired
    private ProductServiceClient productClient;

    @CircuitBreaker(name = "productservice", fallbackMethod = "requestProductFallBack")
    public ResponseEntity<ProductDTO> requestProduct(ProductDTO product){
        return productClient.requestProduct(product);
    }

    public ResponseEntity<ProductDTO> requestProductFallBack(Throwable throwable){
        System.out.println("Circuit breaker (requestProduct): " + throwable.getMessage());
        return null;
    }

    @CircuitBreaker(name = "productservice", fallbackMethod = "increaseQuantityFallBack")
    public ResponseEntity<List<ProductDTO>> increaseQuantity(List<Item> items){
        return productClient.increaseQuantity(items);
    }

    public ResponseEntity<ProductDTO> increaseQuantityFallBack(Throwable throwable){
        System.out.println("Circuit breaker (increaseQuantity): " + throwable.getMessage());
        return null;
    }

}
