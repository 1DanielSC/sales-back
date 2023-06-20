package com.salesback.service.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

import com.salesback.model.Item;
import com.salesback.model.dto.ProductDTO;

@HttpExchange
public interface ProductServiceClient {
    @PutExchange("/product/request")
    ResponseEntity<ProductDTO> requestProduct(@RequestBody ProductDTO product);

    @PutExchange("/product/products")
    ResponseEntity<List<ProductDTO>> increaseQuantity(@RequestBody List<Item> items);
}
