package com.fullcars.restapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@Entity
//@Table(name = "sale_details")
public class SaleDetail extends BaseDetail{
	
    @JsonBackReference
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id")
    @ToString.Exclude  // <--- ¡AGREGA ESTO! Rompe el ciclo hacia arriba
    private Sale sale;

    @Column(name = "printed_description", nullable = true)
    private String printedDescription;

    public String getFacturaDescription() {
    	if (this.printedDescription != null && !this.printedDescription.isEmpty()) {
            return this.printedDescription;
        }

        if (this.getCarPart() != null) {
        	return getCarPart().getName()+ "  " +((getCarPart().getDescription() != null)? getCarPart().getDescription() : "");
        }
        
        return "ARTÍCULO SIN DESCRIPCIÓN";
    }
    
}