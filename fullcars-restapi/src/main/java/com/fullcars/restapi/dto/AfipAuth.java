package com.fullcars.restapi.dto;


import java.time.LocalDateTime;

public class AfipAuth {
    private final String token;
    private final String sign;
    private final LocalDateTime expiration;

    public AfipAuth(String token, String sign, LocalDateTime expiration) {
        this.token = token;
        this.sign = sign;
        this.expiration = expiration;
    }

    public String getToken() { return token; }
    public String getSign() { return sign; }
    public LocalDateTime getExpirationTime() { return expiration; }
}

