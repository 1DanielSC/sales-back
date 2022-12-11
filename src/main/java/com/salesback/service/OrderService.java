package com.salesback.service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.salesback.model.Order;
import com.salesback.model.Product;
import com.salesback.model.dto.ProductDTO;
import com.salesback.model.enums.EnumOrderType;
import com.salesback.repository.OrderRepository;
import com.salesback.service.interfaces.ProductServiceInterface;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    private ProductServiceInterface repository;

    @Autowired
    public OrderService(ProductServiceInterface psi){
        this.repository = psi;
    }

    public Order findById(Long id){
        Optional<Order> order = orderRepository.findById(id);
        return order.isPresent() ? order.get() : null;
    }

    public List<Order> findAll(){
        return orderRepository.findAll();
    }

    public Order save(Order order){
        order.setDate(new Date(System.currentTimeMillis()));
        return orderRepository.save(order);
    }

    @CircuitBreaker(name = "servicebeta", fallbackMethod = "buildFallBack")
    @Retry(name = "retryservicebeta", fallbackMethod = "retryFallBack")
    @Bulkhead(name = "bulkheadservicebeta", type = Bulkhead.Type.THREADPOOL, fallbackMethod = "bulkheadFallBack")
    public ResponseEntity<Product> getProductByName(String productName){
        return repository.findProductByName(productName);
    }

    @CircuitBreaker(name = "servicebeta", fallbackMethod = "buildFallBack")
    @Retry(name = "retryProductService", fallbackMethod = "retryFallBack")
    @Bulkhead(name = "bulkheadservicebeta", fallbackMethod = "bulkheadFallBack")
    public ResponseEntity<Product> updateProduct(Product product){
        return repository.updateProduct(product);
    }

    public ResponseEntity<String> bulkheadFallBack(Exception t){
        System.out.println("BULKHEAD - Falha no product ");
        return ResponseEntity.ok("failllllllllll");
    } 

    public ResponseEntity<String> retryProductService(Exception t){
        System.out.println("SERVIÇO CAIU - Falha no product ");
        return ResponseEntity.ok("failllllllllll");
    }    

    public ResponseEntity<String> buildFallBack(Exception t){
        System.out.println("Falha no product");
        return ResponseEntity.ok("failllllllllll");
    }

    public Order sellProduct(ProductDTO product){
        ResponseEntity<Product> response = getProductByName(product.getName());
        
        if(response.getStatusCode() == HttpStatus.OK){
            Product productReceived = response.getBody();
            
            if(productReceived != null){
                
                if(productReceived.getQuantity() < product.getQuantity()){
                    System.out.println("Quantity greater than available");
                    return null;
                }

                productReceived.setQuantity(productReceived.getQuantity() - product.getQuantity());
                ResponseEntity<Product> requestUpdate = updateProduct(productReceived);
                
                if(requestUpdate.getStatusCode() == HttpStatus.OK){    
                    Order order = new Order();
                    order.setProductName(productReceived.getName());
                    order.setProductPrice(productReceived.getPrice());
                    order.setQuantity(product.getQuantity());
                    order.setType(EnumOrderType.SELL);
                    return save(order);
                }
            }

            System.out.println("Error on selling product.");
            return null;
        }
        else{
            System.out.println("Product not Found!");
            return null;
        }
    }

    public Order buyProduct(ProductDTO product){
        ResponseEntity<Product> response = getProductByName(product.getName());

        if(response.getStatusCode() == HttpStatus.OK){
            Product productReceived = response.getBody();

            if(productReceived != null){

                productReceived.setQuantity(productReceived.getQuantity() + product.getQuantity());
                ResponseEntity<Product> requestUpdate = updateProduct(productReceived);

                if(requestUpdate.getStatusCode() == HttpStatus.OK){
                    Order order = new Order();
                    order.setProductName(product.getName());
                    order.setQuantity(product.getQuantity());
                    order.setProductPrice(product.getPrice());
                    order.setType(EnumOrderType.BUY);
                    return save(order);
                }
            }

            System.out.println("Error on buying product.");
            return null;
        }
        else{
            System.out.println("Product not Found!");
            return null;
        }

    }

}
