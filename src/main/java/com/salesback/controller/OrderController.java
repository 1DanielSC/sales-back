package com.salesback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.salesback.model.Item;
import com.salesback.model.Order;
import com.salesback.model.enums.EnumStatusOrder;
import com.salesback.service.OrderService;

@Controller
@RequestMapping(value = "order")
public class OrderController {
    
    @Autowired
    private OrderService service;

    @GetMapping
    public ResponseEntity<List<Order>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Order> findById(@PathVariable(value = "id") Long orderId){
        Order order = service.findById(orderId);

        if(order != null)
            return ResponseEntity.ok(order);
        else
            return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody(required = true) Order entity){
        return ResponseEntity.ok(service.createOrder(entity));
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<Order> updateStatus(@PathVariable(value = "id") Long orderId, @PathVariable(value = "status") EnumStatusOrder status){
        return ResponseEntity.ok(service.updateStatus(orderId, status));
    }

    @PostMapping("/{id}/item")
    public ResponseEntity<Order> addItemToOrder(@PathVariable(value = "id") Long orderId, @RequestBody(required = true) Item item){
        return ResponseEntity.ok(service.addItemToOrder(orderId, item));
    }
}
