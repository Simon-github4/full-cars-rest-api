package com.fullcars.restapi.dto;

import java.math.BigDecimal;


public record ProviderPartDTO(String nombre, String marca, BigDecimal precio, Long providerId) {}

