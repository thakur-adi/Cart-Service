package dev.aditya.cartservice.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequestDTO {
    private String deliveryAddress;
    private String paymentMethod;
}
