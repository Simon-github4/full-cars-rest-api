package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private int quantity;
    @Column(precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @ManyToOne(optional = false)
    private CarPart carPart;
    
    public BigDecimal getSubTotal() {
    	return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP) ;
    }
}
