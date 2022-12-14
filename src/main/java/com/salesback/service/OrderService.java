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

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;

    private OrderResilience orderResilience;

    @Autowired
    public OrderService(OrderResilience orderResilience){
        this.orderResilience = orderResilience;
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

    public Order sellProduct(ProductDTO product){
        ResponseEntity<Product> response = orderResilience.getProductByName(product.getName());
        
        if(response.getStatusCode() == HttpStatus.OK){
            Product productReceived = response.getBody();
            
            if(productReceived != null){
                
                if(productReceived.getQuantity() < product.getQuantity()){
                    System.out.println("Quantity greater than available");
                    return null;
                }

                productReceived.setQuantity(productReceived.getQuantity() - product.getQuantity());
                ResponseEntity<Product> requestUpdate = orderResilience.updateProduct(productReceived);
                
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
        ResponseEntity<Product> response = orderResilience.getProductByName(product.getName());

        if(response.getStatusCode() == HttpStatus.OK){
            Product productReceived = response.getBody();

            if(productReceived != null){

                productReceived.setQuantity(productReceived.getQuantity() + product.getQuantity());
                ResponseEntity<Product> requestUpdate = orderResilience.updateProduct(productReceived);

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
