package com.salesback.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.salesback.model.enums.EnumStatusOrder;

@Entity
@Table(name = "orders")
public class Order implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Item> items;
    
    private Double totalPrice;
    
    private EnumStatusOrder status;
    
    private LocalDateTime date;

    public Order(){
        this.totalPrice = 0.0;
        this.status = EnumStatusOrder.CREATED;
        this.items = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public EnumStatusOrder getStatus() {
        return status;
    }

    public void setStatus(EnumStatusOrder status) {
        this.status = status;
    }
    
    
    
    
}
