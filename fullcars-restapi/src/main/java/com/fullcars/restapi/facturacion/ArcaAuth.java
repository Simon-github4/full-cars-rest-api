package com.fullcars.restapi.facturacion;

import java.time.LocalDateTime;

public class ArcaAuth {
    private final String token;
    private final String sign;
    private final LocalDateTime expiration;

    public ArcaAuth(String token, String sign, LocalDateTime expiration) {
        this.token = token;
        this.sign = sign;
        this.expiration = expiration;
    }

    public String getToken() { return token; }
    public String getSign() { return sign; }
    public LocalDateTime getExpirationTime() { return expiration; }
}

