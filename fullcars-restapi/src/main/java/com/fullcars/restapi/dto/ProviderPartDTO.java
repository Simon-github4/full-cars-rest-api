package com.fullcars.restapi.dto;

import java.math.BigDecimal;
import com.fullcars.restapi.model.ProviderPart;

public record ProviderPartDTO(
        String nombre,
        String marca,
        BigDecimal precio,
        Long providerId,
        String provCod,
        String quality,
        String category
) {

    // Factory method to build the DTO from an entity
    public static ProviderPartDTO fromEntity(ProviderPart part) {
        return new ProviderPartDTO(
                part.getNombre(),
                part.getMarca(),
                part.getPrecio(),
                part.getProviderMapping().getProviderId(),
                part.getProvCod(),
                part.getQuality(),
                part.getCategory() 
        );
    }

    // Optional: convert back to entity if needed
    public ProviderPart toEntity() {
        ProviderPart part = new ProviderPart();
        part.setNombre(nombre);
        part.setMarca(marca);
        part.setPrecio(precio);
        part.setProvCod(provCod);
        part.setQuality(quality);
        // Provider and Category can be set later by service logic
        return part;
    }
}
