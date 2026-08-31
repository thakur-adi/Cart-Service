package dev.aditya.cartservice.Validation;

import dev.aditya.cartservice.Exception.EmptyCartException;
import dev.aditya.cartservice.Model.Cart;
import dev.aditya.cartservice.Repository.CartRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
public class Validator {

    @Autowired
    CartRepo cartRepo;

    public Cart getValidCart(Long userId){
        Optional<Cart> optionalCart = cartRepo.findCartByUserIdAndIsActive(userId,Boolean.TRUE);
        if(optionalCart.isEmpty()){
            throw new EmptyCartException("Your cart is Empty! Please add something to you cart first!!");
        }
        return optionalCart.get();
    }
}
