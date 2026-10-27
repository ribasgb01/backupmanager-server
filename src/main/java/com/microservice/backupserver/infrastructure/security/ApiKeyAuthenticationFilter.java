package com.microservice.backupserver.infrastructure.security;

import com.microservice.backupserver.application.gateways.MarketGateway;
import com.microservice.backupserver.domain.Market;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final MarketGateway marketGateway;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");

        if(apiKey == null || apiKey.isBlank()){
            filterChain.doFilter(request, response);
            return;
        }

        Optional<Market> optionalMarket = marketGateway.findByApiKey(apiKey);

        if(optionalMarket.isEmpty() || !optionalMarket.get().getActive()){
            filterChain.doFilter(request, response);
            return;
        }

        Market market = optionalMarket.get();

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_AGENT"));

        var apiKeyAuthenticationToken = new ApiKeyAuthenticationToken(market, authorities);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(apiKeyAuthenticationToken);
            SecurityContextHolder.setContext(context);
        }

        filterChain.doFilter(request, response);
    }
}
