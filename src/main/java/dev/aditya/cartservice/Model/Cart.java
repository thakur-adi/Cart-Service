package dev.aditya.cartservice.Model;

import dev.aditya.cartservice.Dto.CartResponseDTO;
import dev.aditya.cartservice.Dto.ProductResponseDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "carts", indexes = {@Index(name = "idx_user_id", columnList = "userId")})
public class Cart extends Base{

    private Long userId;//We index the cart table based on userId so that it's quicker to query on.

    @Setter(value = AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.REMOVE)
    //Unfortunately there needs to be a relation between these 2 classes as it's a list and RelationalDB can't store lists as a value(violates 1-nf). So we have to make product also an entity and define a relation between these 2 entities.
    private Map<Long, Product> products = new HashMap<>();

    private Double totalAmount =0.0;

    public CartResponseDTO convertToCartDto() {
        CartResponseDTO cartResponseDTO = new CartResponseDTO();
        cartResponseDTO.setTotalAmount(totalAmount);
        List<ProductResponseDTO> productList = new ArrayList<>();
        for(Product product: products.values()){
            productList.add(product.convertToDto());
        }
        cartResponseDTO.setProducts(List.copyOf(productList));
        return cartResponseDTO;
    }

    public void addProduct(Product product){
        products.put(product.getProductId(),product);
    }

    public void removeProduct(Product product){
        products.remove(product.getProductId());
    }
}
