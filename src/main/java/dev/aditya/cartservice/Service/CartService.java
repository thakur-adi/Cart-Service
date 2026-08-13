package dev.aditya.cartservice.Service;

import dev.aditya.cartservice.Dto.CheckoutRequestDTO;
import dev.aditya.cartservice.Dto.OrderDTO;
import dev.aditya.cartservice.Dto.ProductCatalogDTO;
import dev.aditya.cartservice.Dto.CartRequestDTO;
import dev.aditya.cartservice.Exception.EmptyCartException;
import dev.aditya.cartservice.Model.Cart;
import dev.aditya.cartservice.Model.Product;
import dev.aditya.cartservice.Repository.CartRepo;
import dev.aditya.cartservice.Validation.Validator;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class CartService implements ICartService{

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Validator validator;

    @Override
    public Cart viewCart(Long userId) {
        return validator.getValidCart(userId);
    }

    @Override
    public Cart addToCart(Long userId, CartRequestDTO cartRequestDTO) {
        Cart cart;
        Optional<Cart> optionalCart = cartRepo.findCartByUserIdAndIsActive(userId,Boolean.TRUE);
        if(optionalCart.isEmpty()){
            cart = new Cart();
            cart.setUserId(userId);
            addNewProductToCart(cart, cartRequestDTO);
        }
        else{
            cart = optionalCart.get();
            if(cart.getProducts().containsKey(cartRequestDTO.getProductId())){
                Product product = cart.getProducts().get(cartRequestDTO.getProductId());
                product.setQuantity(product.getQuantity()+ cartRequestDTO.getQuantity());
                cart.setTotalAmount(cart.getTotalAmount()+(product.getPrice()* cartRequestDTO.getQuantity()));
            }
            else{
                addNewProductToCart(cart, cartRequestDTO);
            }
        }
        return cartRepo.save(cart);
    }

    @Override
    public Cart updateCart(Long userId, CartRequestDTO cartRequestDTO) {
        //Validate the cart
        Cart cart = validator.getValidCart(userId);
        Product product = cart.getProducts().get(cartRequestDTO.getProductId());
        if(cartRequestDTO.getQuantity()==0){
            cart.removeProduct(product);
        }
        else{
            //First reduce the whole product's contribution
            cart.setTotalAmount(cart.getTotalAmount()-(product.getPrice()* product.getQuantity()));
            //Update the Products quantity
            product.setQuantity(cartRequestDTO.getQuantity());
            cart.addProduct(product);
            //Add the new Products price
            cart.setTotalAmount(cart.getTotalAmount()+(product.getPrice()* product.getQuantity()));
        }
        return cartRepo.save(cart);
    }


    @Override
    public Cart removeFromCart(Long userId, Long productId) {
        Cart cart = validator.getValidCart(userId);
        Product product = cart.getProducts().get(productId);
        cart.removeProduct(product);
        if(cart.getProducts().isEmpty()){
            cart.setIsActive(Boolean.FALSE);
            throw new EmptyCartException("Your cart is now empty!! Please add in some items!");
        }
        cart.setTotalAmount(cart.getTotalAmount()-(product.getPrice()*product.getQuantity()));
        return cartRepo.save(cart);
    }

    @Override
    public void createOrder(Long userID, CheckoutRequestDTO checkoutRequestDTO) {
        Cart cart =validator.getValidCart(userID);
        //Creating an orderDTO first
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setTotalAmount(cart.getTotalAmount());
        orderDTO.setProducts(cart.getProducts().values().stream().toList()); //stream().toList() return an unmodifiable list
        orderDTO.setDeliveryAddress(checkoutRequestDTO.getDeliveryAddress());
        orderDTO.setPaymentMethod(checkoutRequestDTO.getPaymentMethod());


        //An HTTP call to initiate a new order.
        String baseUrl = "http://Order-Service/order/new";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-User-Id",String.valueOf(userID));

        HttpEntity<OrderDTO> requestEntity = new HttpEntity<>(orderDTO,headers);

        ResponseEntity<Void> orderResponseEntity = restTemplate.postForEntity(baseUrl,requestEntity,Void.class);
    }



    //Helper method
    private Product convertDtoToProduct(CartRequestDTO cartRequestDTO) {

        Product product = new Product();
        product.setId(cartRequestDTO.getProductId());
        product.setQuantity(cartRequestDTO.getQuantity());

        //An HTTP call to get the remaining product details.
        String baseUrl = "http://Product-Catalog-Service/products/";
        ResponseEntity<ProductCatalogDTO> productCatalogDTOResponseEntity = restTemplate.getForEntity(baseUrl+ cartRequestDTO.getProductId(), ProductCatalogDTO.class);

        if(productCatalogDTOResponseEntity.getStatusCode().is2xxSuccessful()){
            ProductCatalogDTO productCatalogDTO = productCatalogDTOResponseEntity.getBody();
            product.setProductName(productCatalogDTO.getName());
            product.setProductImageUrl(productCatalogDTO.getImageUrl());
            product.setPrice(productCatalogDTO.getPrice());
        }
        else {
            throw new InternalServerErrorException("Some internal server issue encountered! Please try again later!!");
        }

        return product;
    }

    private Cart addNewProductToCart(Cart cart, CartRequestDTO cartRequestDTO){
        Product product = convertDtoToProduct(cartRequestDTO);
        cart.addProduct(product);
        cart.setTotalAmount(cart.getTotalAmount()+(product.getPrice()*product.getQuantity()));
        return cart;
    }

}
