package dev.aditya.cartservice.Service;

import dev.aditya.cartservice.Dto.CheckoutRequestDTO;
import dev.aditya.cartservice.Dto.OrderDTO;
import dev.aditya.cartservice.Dto.ProductCatalogDTO;
import dev.aditya.cartservice.Dto.CartRequestDTO;
import dev.aditya.cartservice.Exception.EmptyCartException;
import dev.aditya.cartservice.Exception.ProductNotFoundException;
import dev.aditya.cartservice.Model.Cart;
import dev.aditya.cartservice.Model.Product;
import dev.aditya.cartservice.Repository.CartRepo;
import dev.aditya.cartservice.Repository.ProductRepo;
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
    @Autowired
    private ProductRepo productRepo;

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
        cart.setIsActive(Boolean.TRUE);
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
            //Remove and then add the Products back in cart
            cart.removeProduct(product);
            addNewProductToCart(cart,cartRequestDTO);
        }
        return cartRepo.save(cart);
    }


    @Override
    public Cart removeFromCart(Long userId, Long productId) {
        Cart cart = validator.getValidCart(userId);
        Product product = cart.getProducts().get(productId);
        if(product==null){
            throw new ProductNotFoundException("No Product Found!! The product you are looking for for has already been removed!");
        }
        cart.removeProduct(product);
        if(cart.getProducts().isEmpty()){
            cart.setIsActive(Boolean.FALSE);
            cartRepo.save(cart);
            throw new EmptyCartException("Your cart is now empty!! Please add in some items!");
        }
        cart.setTotalAmount(cart.getTotalAmount()-(product.getPrice()*product.getQuantity()));
        return cartRepo.save(cart);
    }

    @Override
    public ResponseEntity<String> createOrder(String authToken, Long userID, CheckoutRequestDTO checkoutRequestDTO) {

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
        headers.setBearerAuth(authToken.substring(7));

        HttpEntity<OrderDTO> requestEntity = new HttpEntity<>(orderDTO,headers);

        //Just return whatever receiving from order service along with status code etc.
        ResponseEntity<String> paymentLinkResponse =  restTemplate.postForEntity(baseUrl,requestEntity,String.class);

        //Once Payment Link has been generated cart should get deleted or emptied as it has now become an order
        cart.setIsActive(Boolean.FALSE);
        cartRepo.save(cart);
        
        return paymentLinkResponse;
    }



    //Helper method
    private Product convertDtoToProduct(CartRequestDTO cartRequestDTO) {

        Product product = new Product();
        product.setProductId(cartRequestDTO.getProductId());
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
        /*
        This else condition wouldn't work as the call will fail at line 141 itself and throw an HttpServerErrorException
        else {
            throw new InternalServerErrorException("Some internal server issue encountered! Please try again later!!");
        }*/
        Optional<Product> optProduct = productRepo.findProductByProductIdAndQuantityAndPrice(product.getProductId(), product.getQuantity(), product.getPrice());
        if(optProduct.isEmpty()){
            return productRepo.save(product);
        }
        else {
            return optProduct.get();
        }
    }

    private Cart addNewProductToCart(Cart cart, CartRequestDTO cartRequestDTO){
        Product product = convertDtoToProduct(cartRequestDTO);
        cart.addProduct(product);
        //update the cart total amount
        cart.setTotalAmount(cart.getTotalAmount()+(product.getPrice()*product.getQuantity()));
        return cart;
    }

}
