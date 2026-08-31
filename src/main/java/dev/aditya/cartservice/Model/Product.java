package dev.aditya.cartservice.Model;

import dev.aditya.cartservice.Dto.ProductResponseDTO;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
// Unfortunately we'll have to make this an entity as well otherwise it won't compile or run the service(this is mostly because we are using RelationalDB).
// We won't be able to store this as a list in Order table as it would violate 1-nf property of RelationalDB.
// And just so that it doesn't get overwritten by JPA when some other order has the same product of different quantity we'll extend it from base class,so that every product has its own unique id(other than productId) and quantity.
public class Product extends Base{
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private Double price;

    public ProductResponseDTO convertToDto(){
        ProductResponseDTO productResponseDTO =new ProductResponseDTO();
        productResponseDTO.setProductName(productName);
        productResponseDTO.setPrice(price);
        productResponseDTO.setQuantity(quantity);
        productResponseDTO.setProductImageUrl(productImageUrl);
        return productResponseDTO;
    }
}
