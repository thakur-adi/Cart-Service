package dev.aditya.cartservice.Controller;

import dev.aditya.cartservice.Dto.CartResponseDTO;
import dev.aditya.cartservice.Dto.CheckoutRequestDTO;
import dev.aditya.cartservice.Dto.CartRequestDTO;
import dev.aditya.cartservice.Dto.OrderDTO;
import dev.aditya.cartservice.Model.Cart;
import dev.aditya.cartservice.Service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> viewCartItems(){
        Cart userCart =  cartService.viewCart(getUserId());
       return new ResponseEntity<>(userCart.convertToCartDto(), HttpStatus.FOUND);
    }

    //This will be called when the user hits "Add to cart" button on product page.
    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addItemsToCart(@RequestBody CartRequestDTO cartRequestDto){
        Cart userCart = cartService.addToCart(getUserId(), cartRequestDto);
        return new ResponseEntity<>(userCart.convertToCartDto(), HttpStatus.OK);
    }

    //This will be called when user updates the quantity of any product already present in the cart on cart page
    @PutMapping("/update")
    public ResponseEntity<CartResponseDTO> updateItemsFromCart(@RequestBody CartRequestDTO cartRequestDto){
        Cart userCart = cartService.updateCart(getUserId(),cartRequestDto);
        return  new ResponseEntity<>(userCart.convertToCartDto(), HttpStatus.OK);
    }

    //This is called when you hit "delete/remove" button on Cart page
    @PutMapping("/remove/{id}")
    public ResponseEntity<CartResponseDTO> removeItemsFromCart(@PathVariable("id") Long productId){
        Cart userCart = cartService.removeFromCart(getUserId(),productId);
        return  new ResponseEntity<>(userCart.convertToCartDto(), HttpStatus.OK);
    }

    @GetMapping("/checkout")
    public ResponseEntity<String> createNewOrder(@RequestBody CheckoutRequestDTO checkoutRequestDTO){
        //call -> order system -> which will internally call -> Payment System
        cartService.createOrder(getUserId(), checkoutRequestDTO);
        return new ResponseEntity<>("Do not Refresh!! Order is now being processed!",HttpStatus.OK);
    }


    //Helper Methods

    private Long getUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
