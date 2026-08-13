package dev.aditya.cartservice.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDTO {
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private Double price;
}
