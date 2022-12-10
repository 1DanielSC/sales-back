package com.salesback.service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.salesback.model.Order;
import com.salesback.model.Product;
import com.salesback.model.dto.ProductDTO;
import com.salesback.model.enums.EnumOrderType;
import com.salesback.repository.OrderRepository;
import com.salesback.repository.ProductRepository;
import com.salesback.service.interfaces.ProductServiceInterface;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

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

    @CircuitBreaker(name = "servicebeta")
    public ResponseEntity<Product> getProductByName(String productName){
        return repository.findProductByName(productName);
    }

    @CircuitBreaker(name = "servicebeta")
    public ResponseEntity<Product> updateProduct(Product product){
        return repository.updateProduct(product);
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
        RestTemplate restTemplate = new RestTemplate();
        String url_getProduct = "http://localhost:8080/product/findByName/";

        ResponseEntity<Product> response = restTemplate.getForEntity(url_getProduct + product.getName(), Product.class);

        if(response.getStatusCode() == HttpStatus.OK){
            Product productReceived = response.getBody();

            if(productReceived != null){
                String url_updateProduct = "http://localhost:8080/product/update";
                productReceived.setQuantity(productReceived.getQuantity() + product.getQuantity());

                HttpEntity<Product> request = new HttpEntity<>(productReceived);
                ResponseEntity<Product> requestUpdate = restTemplate.exchange(url_updateProduct, HttpMethod.PUT, request, Product.class);
                
                if(requestUpdate.getStatusCode() == HttpStatus.OK){
                    Order order = new Order();

                    if(!checkIfPresentByName(productReceived)){
                        productRepository.save(productReceived);
                    }

                    //order.setProduct(productReceived);
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


    public boolean checkIfPresentByName(Product product){
        return productRepository.findByName(product.getName()).isPresent();
    }
}
