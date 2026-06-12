package com.fullcars.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@DiscriminatorValue("FACTURA")
public class Factura extends Comprobante {@Override
	
	@JsonIgnore
	public String getTextoTitulo() {
		return "FACTURA";
	}

	@JsonIgnore
	@Override
	public String getComprobanteAsociadoToPDF() {
		return null;
	}
}
