package dev.aditya.cartservice.Service;

import dev.aditya.cartservice.Dto.CartRequestDTO;
import dev.aditya.cartservice.Dto.CheckoutRequestDTO;
import dev.aditya.cartservice.Model.Cart;
import org.springframework.http.ResponseEntity;

public interface ICartService {


    Cart viewCart(Long userId);

    Cart addToCart(Long userId, CartRequestDTO cartRequestDTO);

    Cart updateCart(Long userId, CartRequestDTO cartRequestDTO);

    Cart removeFromCart(Long userId, Long productId);

    ResponseEntity<String> createOrder(Long userID, CheckoutRequestDTO checkoutRequestDTO);
}
