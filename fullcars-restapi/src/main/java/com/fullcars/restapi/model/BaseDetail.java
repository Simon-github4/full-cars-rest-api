package com.fullcars.restapi.model;

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
public abstract class BaseDetail {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
    private Integer quantity;
    private Long unitPrice;
    
    @ManyToOne(optional = false)
    private CarPart product;
    
    public abstract String getTableName() ;
    
}
