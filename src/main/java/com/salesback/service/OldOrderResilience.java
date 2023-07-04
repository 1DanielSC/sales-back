package com.salesback.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.salesback.model.Item;
import com.salesback.model.dto.ProductDTO;
import com.salesback.service.interfaces.ProductServiceInterface;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class OldOrderResilience {
    
    private ProductServiceInterface repository;

    @Autowired
    public OldOrderResilience(ProductServiceInterface repository){
        this.repository = repository;
    }

    @CircuitBreaker(name = "updateProductBreaker", fallbackMethod = "buildFallBackUpdate")
    @Retry(name = "retryservicebeta", fallbackMethod = "retryFallBack")
    @Bulkhead(name = "updateProductBulk", fallbackMethod = "buildBulkheadUpdate")
    public ResponseEntity<ProductDTO> requestProduct(ProductDTO product){
        return repository.requestProduct(product);
    }

    @CircuitBreaker(name = "updateProductBreaker", fallbackMethod = "buildFallBackUpdate")
    @Retry(name = "retryservicebeta", fallbackMethod = "retryFallBack")
    @Bulkhead(name = "updateProductBulk", fallbackMethod = "buildBulkheadUpdate")
    public ResponseEntity<List<ProductDTO>> increaseQuantity(List<Item> items){
        return repository.increaseQuantity(items);
    }

    //BULKHEAD - FALLBACK
    public ResponseEntity<String> bulkheadFallBack(String productName, Throwable t){
        System.out.println("BULKHEAD (GET) - Falha no product " + productName +"\n\n");
        return ResponseEntity.ok("Fail: BULKHEAD (GET)");
    } 

    public ResponseEntity<String> buildBulkheadUpdate(ProductDTO product, Throwable t){
        System.out.println(t.getMessage());
        System.out.println(t.getCause());

        System.out.println("\n\nBULKHEAD (UPDATE) - Falha no product " + product.getName() +"\n\n");
        return ResponseEntity.internalServerError().build();
    }

    //RETRY - FALLBACK
    public ResponseEntity<String> retryFallBack(Throwable t){
        System.out.println("SERVIÇO CAIU - Falha no product ");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }    

    //CIRCUIT BREAKER - FALLBACK 
    public ResponseEntity<String> buildFallBack(String productName, Throwable t){
        System.out.println("\n\nCIRCUIT BREAKER (GET): Falha no product " + productName+"\n\n");
        return ResponseEntity.ok("Fail: CIRCUIT BREAKER (GET)");
    }

    public ResponseEntity<String> buildFallBackUpdate(ProductDTO product, Throwable t){
        System.out.println("\n\nCIRCUIT BREAKER (UPDATE): Falha no product " + product.getName()+"\n\n");
        return ResponseEntity.ok("Fail: CIRCUIT BREAKER (UPDATE)");
    }
}
