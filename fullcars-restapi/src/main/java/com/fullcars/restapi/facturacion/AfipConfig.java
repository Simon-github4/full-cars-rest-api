package com.fullcars.restapi.facturacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AfipConfig {
    @Value("${afip.wsaa.endpoint}")
    private String wsaaEndpoint;

    // Getters...
    public String getWsaaEndpoint() { return wsaaEndpoint; }
}
