package com.fullcars.restapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class CarPart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    
    @NotBlank(message = "El SKU no puede estar vacío")//ppor si se aplica @Valid
    @Column(unique = true, nullable = false)
    private String sku;
	
    @Builder.Default
    private Long stock = 0L;

    @ManyToOne//(fetch=FetchType.LAZY)  -----> Problems with JSON Response
    private Category category;
    @ManyToOne
    private Brand brand;
    @ManyToOne
    private Provider provider;
    
    @Builder.Default
    private Long basePrice = 0L;
    //private int purchasePrice;
}