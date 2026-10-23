package com.microservice.backupserver.infrastructure.security;

import com.microservice.backupserver.domain.Market;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final Market principal;

    public ApiKeyAuthenticationToken(Market market, Collection<? extends GrantedAuthority> authorities){
        super(authorities);
        this.principal = market;
        setAuthenticated(true);
    }


    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return this.principal;
    }
}
