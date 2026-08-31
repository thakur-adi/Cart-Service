package dev.aditya.cartservice.Dto;

import dev.aditya.cartservice.Model.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderDTO {
    private List<Product> products;
    private Double totalAmount;
    private String deliveryAddress;
    private String paymentMethod;
}
