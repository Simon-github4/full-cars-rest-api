package com.fullcars.restapi.dto;

import java.time.LocalDate;

import com.fullcars.restapi.enums.MovementType;

public record StockMovementDTO(
	    Long id,
	    Integer quantity,
	    LocalDate date,
	    String reference,
	    String carPartSku,
	    MovementType type
	) {}