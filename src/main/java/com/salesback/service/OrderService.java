package com.salesback.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.salesback.exceptions.APIConnectionError;
import com.salesback.exceptions.BadRequestException;
import com.salesback.exceptions.GenericException;
import com.salesback.exceptions.NotFoundException;
import com.salesback.model.Item;
import com.salesback.model.Order;
// import com.salesback.model.Product;
import com.salesback.model.dto.ProductDTO;
import com.salesback.model.enums.EnumStatusOrder;
import com.salesback.repository.OrderRepository;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository repository;

    private OrderResilience orderResilience;

    @Autowired
    public OrderService(OrderResilience orderResilience){
        this.orderResilience = orderResilience;
    }

    public Order findById(Long id){
        Optional<Order> order = repository.findById(id);
        return order.isPresent() ? order.get() : null;
    }

    public List<Order> findAll(){
        return repository.findAll();
    }

    public Order updateStatus(Long id, EnumStatusOrder status){

        Order order = findById(id);
        if(order==null)
            throw new NotFoundException("Order not found.");
        
        if(status == EnumStatusOrder.APPROVED)
            return placeOrder(order);
        else if(status == EnumStatusOrder.CANCELED)
            return cancelOrder(order);
        else if(status == EnumStatusOrder.REFUSED)
            return refuseOrder(order);

        throw new BadRequestException("Invalid status.");
    }


    public Order createOrder(Order order){
        order.setId(null);
        order.setDate(LocalDateTime.now());
        order.setStatus(EnumStatusOrder.CREATED);
      
        return repository.save(order);
    }

    public Order addItemToOrder(Long orderId, Item item){
        Order order = findById(orderId);

        if(order==null)
            throw new NotFoundException("Order not found.");
        
        ProductDTO product = new ProductDTO();
        product.setName(item.getName());
        product.setQuantity(item.getQuantity());
        
        ResponseEntity<ProductDTO> response = orderResilience.requestProduct(product);
        if(response.getStatusCode()==HttpStatus.SERVICE_UNAVAILABLE)
            throw new APIConnectionError("Communication with product-back failed.");
        else if(response.getStatusCode()==HttpStatus.NOT_FOUND)// || response.getBody() == null
            throw new NotFoundException("Product not found.");
        else if(response.getStatusCode()!=HttpStatus.OK)
            throw new GenericException("Request to product server failed.");
        

        product = response.getBody();
        item.setPrice(product.getPrice());

        List<Item> items = order.getItems();
        for (Item item2 : items) {
            if(item2.getName()==item.getName()){
                item2.setQuantity(item2.getQuantity()+item.getQuantity());
                Double priceItem = product.getPrice()*item.getQuantity();
                order.setTotalPrice(order.getTotalPrice()+priceItem);
                return repository.save(order);
            }
        }

        items.add(item);
        order.setItems(items);
        Double priceItem = product.getPrice()*item.getQuantity();
        order.setTotalPrice(order.getTotalPrice()+priceItem);
        return repository.save(order);
    }

    private Order placeOrder(Order order){            
        order.setStatus(EnumStatusOrder.APPROVED);
        return repository.save(order);
    }

    private Order cancelOrder(Order order){
        ResponseEntity<List<ProductDTO>> response = orderResilience.increaseQuantity(order.getItems());
        if(response.getStatusCode()!=HttpStatus.OK)
            throw new GenericException("Request to product server failed.");

        order.setStatus(EnumStatusOrder.CANCELED);
        return repository.save(order);
    }

    private Order refuseOrder(Order order){
        ResponseEntity<List<ProductDTO>> response = orderResilience.increaseQuantity(order.getItems());
        if(response.getStatusCode()!=HttpStatus.OK)
            throw new GenericException("Request to product server failed.");

        order.setStatus(EnumStatusOrder.REFUSED);
        return repository.save(order);
    }


}
