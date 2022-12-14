package com.salesback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.salesback.model.Product;
import com.salesback.service.interfaces.ProductServiceInterface;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class OrderResilience {
    
    private ProductServiceInterface repository;

    @Autowired
    public OrderResilience(ProductServiceInterface repository){
        this.repository = repository;
    }

    @CircuitBreaker(name = "getProductNameBreaker", fallbackMethod = "buildFallBack")
    @Retry(name = "retryservicebeta", fallbackMethod = "retryFallBack")
    @Bulkhead(name = "getProductNameBulk", fallbackMethod = "bulkheadFallBack")
    public ResponseEntity<Product> getProductByName(String productName){
        return repository.findProductByName(productName);
    }

    @CircuitBreaker(name = "updateProductBreaker", fallbackMethod = "buildFallBackUpdate")
    @Retry(name = "retryservicebeta", fallbackMethod = "retryFallBack")
    @Bulkhead(name = "updateProductBulk", fallbackMethod = "buildBulkheadUpdate")
    public ResponseEntity<Product> updateProduct(Product product){
        return repository.updateProduct(product);
    }

    //BULKHEAD - FALLBACK
    public ResponseEntity<String> bulkheadFallBack(String productName, Throwable t){
        System.out.println("BULKHEAD (GET) - Falha no product " + productName +"\n\n");
        return ResponseEntity.ok("Fail: BULKHEAD (GET)");
    } 

    public ResponseEntity<String> buildBulkheadUpdate(Product product, Throwable t){
        System.out.println("\n\nBULKHEAD (UPDATE) - Falha no product " + product.getName() +"\n\n");
        return ResponseEntity.ok("Fail: BULKHEAD (UPDATE)");
    }

    //RETRY - FALLBACK
    public ResponseEntity<String> retryFallBack(Throwable t){
        System.out.println("SERVIÇO CAIU - Falha no product ");
        return ResponseEntity.ok("failllllllllll");
    }    

    //CIRCUIT BREAKER - FALLBACK 
    public ResponseEntity<String> buildFallBack(String productName, Throwable t){
        System.out.println("\n\nCIRCUIT BREAKER (GET): Falha no product " + productName+"\n\n");
        return ResponseEntity.ok("Fail: CIRCUIT BREAKER (GET)");
    }

    public ResponseEntity<String> buildFallBackUpdate(Product product, Throwable t){
        System.out.println("\n\nCIRCUIT BREAKER (UPDATE): Falha no product " + product.getName()+"\n\n");
        return ResponseEntity.ok("Fail: CIRCUIT BREAKER (UPDATE)");
    }
}
