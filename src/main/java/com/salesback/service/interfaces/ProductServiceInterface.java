package com.salesback.service.interfaces;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.salesback.model.Product;

@FeignClient("PRODUCT")
public interface ProductServiceInterface {
    @RequestMapping(method = RequestMethod.PUT, value = "/product/update")
    ResponseEntity<Product> updateProduct(@RequestBody Product product);

    @RequestMapping(method = RequestMethod.GET, value = "/product/findByName/{name}")
    ResponseEntity<Product> findProductByName(@PathVariable(value = "name") String name);
}
