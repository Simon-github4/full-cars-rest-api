package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Pay {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	@Column(precision = 15, scale = 2)
    private BigDecimal amount;
    private LocalDate date;
    private String paymentMethod;
    
    @ManyToOne
    private Customer customer;
    
    //@ManyToOne
    //private Sale sale;//not confirmed yet
    private String description;

}
