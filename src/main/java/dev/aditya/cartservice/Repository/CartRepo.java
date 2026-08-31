package dev.aditya.cartservice.Repository;

import dev.aditya.cartservice.Model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart,Long> {

    Optional<Cart> findCartByUserIdAndIsActive(Long userId, Boolean isActive);
}
