package dev.aditya.cartservice.Repository;

import dev.aditya.cartservice.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product,Long> {
    Optional<Product> findProductByProductIdAndQuantityAndPrice(Long productId, Integer quantity, Double price);
}
