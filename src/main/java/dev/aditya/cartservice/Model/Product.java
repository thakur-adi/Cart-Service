package dev.aditya.cartservice.Model;

import dev.aditya.cartservice.Dto.ProductResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product {
    private Long id;
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
