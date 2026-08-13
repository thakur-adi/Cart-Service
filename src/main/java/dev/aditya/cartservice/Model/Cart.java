package dev.aditya.cartservice.Model;

import dev.aditya.cartservice.Dto.CartResponseDTO;
import dev.aditya.cartservice.Dto.ProductResponseDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "carts", indexes = {@Index(name = "idx_user_id", columnList = "userId")})
public class Cart extends Base{

    private Long userId;//We index the cart table based on userId so that it's quicker to query on.

    @Setter(value = AccessLevel.NONE)
    private Map<Long, Product> products;

    private Double totalAmount;

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
        products.put(product.getId(),product);
    }

    public void removeProduct(Product product){
        products.remove(product.getId());
    }
}
