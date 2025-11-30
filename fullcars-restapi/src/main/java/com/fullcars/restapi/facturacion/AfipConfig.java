package com.fullcars.restapi.facturacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter@Setter
@Data
public class AfipConfig {
	
	@Value("${wsfev1.service.url}")
	private String wsfev1ServiceUrl; 
	
	@Value("${wsaa.endpoint}")
	private String wsaaEndpoint; 
	
    @Value("${padron.endpoint}")
    private String padronEndpoint;
    
    @Value("${wsfev1.endpoint}")
    private String wsfev1Endpoint;

    @Value("${cuit}")
    private long cuit;
    
    @Value("${crtFile}")
    private String crtFileUrl;
    
    @Value("${keyFile}")
    private String keyFileUrl;
    
    @Value("${TicketTime}")
    private long ticketTime;
    
}
