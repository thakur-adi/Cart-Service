package dev.aditya.cartservice.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartResponseDTO {
    private List<ProductResponseDTO> products;
    private Double totalAmount;
}

