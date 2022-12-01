package com.salesback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.salesback.model.Order;
import com.salesback.model.dto.ProductDTO;
import com.salesback.service.OrderService;

@Controller
@RequestMapping(value = "order")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @GetMapping(value = "/findAll")
    public ResponseEntity<List<Order>> findAll(){
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping(value = "/findById/{id}")
    public ResponseEntity<Order> findOrderById(@PathVariable(value = "id") Long orderId){
        Order order = orderService.findById(orderId);

        if(order != null)
            return ResponseEntity.ok(order);
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/sell")
    public ResponseEntity<Order> sellProduct(@RequestBody ProductDTO product){
        Order order = orderService.sellProduct(product);

        if(order != null)
            return ResponseEntity.ok(order);
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/buy")
    public ResponseEntity<Order> buyProduct(@RequestBody ProductDTO product){
        Order order = orderService.buyProduct(product);

        if(order != null)
            return ResponseEntity.ok(order);
        else
            return ResponseEntity.notFound().build();
    }

}
