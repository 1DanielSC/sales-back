package com.salesback.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.salesback.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    
}
