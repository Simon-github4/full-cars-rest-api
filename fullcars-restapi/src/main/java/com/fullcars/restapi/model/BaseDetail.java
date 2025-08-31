package com.fullcars.restapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

//@Data
@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class BaseDetail {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    private Integer quantity;
    private Long unitPrice;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id") // <- poné el nombre real de la columna en tu DB
    private CarPart carPart;
    
    public float getSubTotal() {
    	return quantity * unitPrice ;
    }
}
