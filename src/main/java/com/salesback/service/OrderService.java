package com.salesback.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.salesback.service.interfaces.ProductServiceClient;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository repository;

    @Autowired
    private ProductServiceClient productClient;

    private OrderResilience orderResilience;

    @Autowired
    public OrderService(OrderResilience orderResilience){
        this.orderResilience = orderResilience;
    }

    @Cacheable(value = "order", key = "id")
    public Order findById(Long id){
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Order not found."));
    }

    @Cacheable("orders")
    public List<Order> findAll(){
        return repository.findAll();
    }

    @CachePut(value = "order", key = "#entity.id")
    public Order update(Order entity){
        return repository.save(entity);
    }

    public Order updateStatus(Long id, EnumStatusOrder status){
        Order order = findById(id);
        
        if(status == EnumStatusOrder.APPROVED)
            return placeOrder(order);
        else if(status == EnumStatusOrder.CANCELED)
            return cancelOrder(order);
        else if(status == EnumStatusOrder.REFUSED)
            return refuseOrder(order);

        throw new BadRequestException("Invalid status.");
    }

    @Transactional(readOnly = false)
    public Order createOrder(Order order){
        order.setId(null);
        order.setDate(LocalDateTime.now());
        order.setStatus(EnumStatusOrder.CREATED);
      
        return repository.save(order);
    }

    private ProductDTO requestProduct(ProductDTO product){
        //ResponseEntity<ProductDTO> response = orderResilience.requestProduct(product);
        ResponseEntity<ProductDTO> response = productClient.requestProduct(product);
        if(response.getStatusCode()==HttpStatus.SERVICE_UNAVAILABLE)
            throw new APIConnectionError("Communication with product-back failed.");
        else if(response.getStatusCode()==HttpStatus.NOT_FOUND)// || response.getBody() == null
            throw new NotFoundException("Product not found.");
        else if(response.getStatusCode()!=HttpStatus.OK)
            throw new GenericException("Request to product server failed.");
        if(response.getBody() == null)
            throw new GenericException("This product is no longer available.");
        return response.getBody();
    }

    public Order addItemToOrder(Long orderId, Item item){
        Order order = findById(orderId);

        if(order==null)
            throw new NotFoundException("Order not found.");
        
        ProductDTO product = new ProductDTO();
        product.setName(item.getName());
        product.setQuantity(item.getQuantity());    

        product = requestProduct(product);
        item.setPrice(product.getPrice());

        List<Item> items = order.getItems();
        for (Item item2 : items) {
            if(item2.getName()==item.getName()){
                item2.setQuantity(item2.getQuantity()+item.getQuantity());
                Double priceItem = product.getPrice()*item.getQuantity();
                order.setTotalPrice(order.getTotalPrice()+priceItem);
                return update(order);
            }
        }

        items.add(item);
        order.setItems(items);
        Double priceItem = product.getPrice()*item.getQuantity();
        order.setTotalPrice(order.getTotalPrice()+priceItem);
        return update(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @CachePut(value = "order", key = "#order.id")
    @Transactional(readOnly = false)
    private Order placeOrder(Order order){            
        order.setStatus(EnumStatusOrder.APPROVED);
        return repository.save(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @CachePut(value = "order", key = "#order.id")
    @Transactional(readOnly = false)
    private Order cancelOrder(Order order){
        ResponseEntity<List<ProductDTO>> response = orderResilience.increaseQuantity(order.getItems());
        if(response.getStatusCode()!=HttpStatus.OK)
            throw new GenericException("Request to product server failed.");

        order.setStatus(EnumStatusOrder.CANCELED);
        return repository.save(order);
    }

    @CacheEvict(value = "orders", allEntries = true)
    @CachePut(value = "order", key = "#order.id")
    @Transactional(readOnly = false)
    private Order refuseOrder(Order order){
        ResponseEntity<List<ProductDTO>> response = orderResilience.increaseQuantity(order.getItems());
        if(response.getStatusCode()!=HttpStatus.OK)
            throw new GenericException("Request to product server failed.");

        order.setStatus(EnumStatusOrder.REFUSED);
        return repository.save(order);
    }


}
