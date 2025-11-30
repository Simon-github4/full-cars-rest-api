package com.fullcars.restapi.facturacion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.facturacion.enums.Servicios;

@Service
public class ArcaTokenCacheService {

	private final Map<Servicios, AfipAuth> tokenCache = new ConcurrentHashMap<>();
	
	@Autowired
	private AfipConfig afipConfig;
	
    public AfipAuth getTicket(Servicios servicio) {
    	AfipAuth ticket = tokenCache.get(servicio);

        if (ticket != null && ticket.esValido()) 
            return ticket;

        // 3. Si no existe o venció, solicitamos uno nuevo (Lento)
        // Usamos synchronized para evitar que dos hilos pidan token al mismo tiempo
        synchronized (this) {
            // Doble chequeo por si otro hilo ya actualizó el token mientras esperábamos
            ticket = tokenCache.get(servicio);
            if (ticket != null && ticket.esValido()) 
                return ticket;

            System.out.println("Solicitando nuevo TAA a AFIP para: " + servicio.nombre());
            AfipAuth nuevoTicket = solicitarNuevoTicketAfiip(servicio);
            
            tokenCache.put(servicio, nuevoTicket);
            
            return nuevoTicket;
        }
    }

    private AfipAuth solicitarNuevoTicketAfiip(Servicios servicio) {
        try {
            return WSAAClient.authenticate(servicio, afipConfig.getWsaaEndpoint(), afipConfig.getCrtFileUrl(),
            		afipConfig.getKeyFileUrl(), afipConfig.getTicketTime());
            
        } catch (Exception e) {
            // Es buena práctica lanzar una RuntimeException personalizada para que Spring la maneje
            throw new RuntimeException("Error fatal obteniendo token para " + servicio.nombre(), e);
        }
    }
    
}
