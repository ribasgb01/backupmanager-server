package com.microservice.backupserver.infrastructure.security;

import com.microservice.backupserver.application.gateways.TokenProviderGateway;
import com.microservice.backupserver.application.gateways.TokenStateGateway;
import com.microservice.backupserver.domain.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProviderGateway tokenProviderGateway;
    private final TokenStateGateway tokenStateGateway;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();

        if(cookies == null){
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = Arrays.stream(cookies)
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if(accessToken == null){
            filterChain.doFilter(request, response);
            return;
        }

        if(!tokenProviderGateway.validateAccessToken(accessToken)){
            filterChain.doFilter(request, response);
            return;
        }

        UUID sessionId = tokenProviderGateway.extractSessionId(accessToken);

        if(tokenStateGateway.isSessionRevoked(sessionId)){
            filterChain.doFilter(request, response);
            return;
        }

        UUID userId = tokenProviderGateway.extractUserId(accessToken);
        String role = tokenProviderGateway.extractRole(accessToken);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        if(SecurityContextHolder.getContext().getAuthentication() == null){
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
    }


}
