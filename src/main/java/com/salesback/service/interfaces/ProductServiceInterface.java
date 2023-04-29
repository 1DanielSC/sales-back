package com.salesback.service.interfaces;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.salesback.model.Item;
import com.salesback.model.dto.ProductDTO;

@FeignClient("PRODUCT")
public interface ProductServiceInterface {

    @RequestMapping(method = RequestMethod.PUT, value = "/product/request")
    ResponseEntity<ProductDTO> requestProduct(@RequestBody ProductDTO product);

    @RequestMapping(method = RequestMethod.PUT, value = "/product/products")
    ResponseEntity<List<ProductDTO>> increaseQuantity(@RequestBody List<Item> items);
}
