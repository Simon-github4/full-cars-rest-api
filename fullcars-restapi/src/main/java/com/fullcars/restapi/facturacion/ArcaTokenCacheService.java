package com.fullcars.restapi.facturacion;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ArcaTokenCacheService {

    /*private final WsaaClient wsaaClient;
    private final Map<String, ArcaAuth> cache = new ConcurrentHashMap<>();

    public ArcaTokenCacheService(WsaaClient wsaaClient) {
        this.wsaaClient = wsaaClient;
    }

    public synchronized ArcaAuth getValidToken(String service) {
    	ArcaAuth token = cache.get(service);

        if (token != null && token.getExpirationTime().isAfter(LocalDateTime.now())) {
            return token;
        }

        // Expirado o inexistente → pedir nuevo
        ArcaAuth newToken = wsaaClient.requestToken(service);
        cache.put(service, newToken);
        return newToken;
    }*/
}
