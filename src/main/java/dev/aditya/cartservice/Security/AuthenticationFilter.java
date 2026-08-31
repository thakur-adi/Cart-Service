package dev.aditya.cartservice.Security;

import dev.aditya.cartservice.Exception.CustomAuthorizationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("LoadBalancedRestTemplate")
    RestTemplate restTemplate;

    @Autowired
    CustomAuthEntryPoint customAuthEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken =request.getHeader(HttpHeaders.AUTHORIZATION);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(authToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<String> authenticatedResponse = restTemplate.postForEntity("http://User-Auth-Service/user/validate",httpEntity, String.class);
        if(authenticatedResponse.getStatusCode().is2xxSuccessful()){
            UsernamePasswordAuthenticationToken authenticationToken =
                    UsernamePasswordAuthenticationToken.authenticated(authenticatedResponse.getHeaders().getFirst("X-USER-ID"),
                                                            null,
                            AuthorityUtils.createAuthorityList(authenticatedResponse.getHeaders().getFirst("X-USER-ROLES")));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(securityContext);

            filterChain.doFilter(request,response);
        }
        else{
            customAuthEntryPoint.commence(request,response, new CustomAuthorizationException("Authentication failed!! Possible Theft!"));
        }
    }
}
