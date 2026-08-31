package dev.aditya.cartservice.Exception;

import org.springframework.security.core.AuthenticationException;

//Gets used in Custom Entry point(Filter Exception Handler)
public class CustomAuthorizationException extends AuthenticationException {

    static String defaultMessage = "You are not Authorized!!";
    public CustomAuthorizationException() {
        super(defaultMessage);
    }
    public CustomAuthorizationException(String message) {
        super(message);
    }
}
