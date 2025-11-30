package com.fullcars.restapi.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AfipAuth {
    private final String token;
    private final String sign;
    private final LocalDateTime expiration;

    public AfipAuth(String token, String sign, LocalDateTime expiration) {
        this.token = token;
        this.sign = sign;
        this.expiration = expiration;
    }

    public boolean esValido() {
        return LocalDateTime.now().isBefore(expiration.minusMinutes(10));
    }
    
    public LocalDateTime getExpirationTime() { return expiration; }
}

