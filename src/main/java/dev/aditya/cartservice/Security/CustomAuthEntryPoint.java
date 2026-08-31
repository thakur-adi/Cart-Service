package dev.aditya.cartservice.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());//Since this is called for JwtAuthentication we can default set it to unauthorized.
        response.setContentType(MediaType.APPLICATION_JSON.toString()); //Tells client that the response incoming is of JSON type
        response.getWriter().write(authException.getMessage());// this sends in the response back to client
    }
}
