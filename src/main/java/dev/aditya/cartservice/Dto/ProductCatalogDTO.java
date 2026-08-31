package dev.aditya.cartservice.Dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public class ProductCatalogDTO {
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private String category;
    private Date createdAt;
}
