package dev.aditya.cartservice.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDTO {
    private Long productId;
    private Integer quantity;

}

