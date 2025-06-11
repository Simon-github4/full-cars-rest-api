package com.fullcars.restapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    private long stock;

    @ManyToOne(fetch=FetchType.LAZY)
    private Category category;
    @ManyToOne(fetch=FetchType.LAZY)
    private Brand brand;

    //private int purchasePrice;
    //private BigDecimal salePrice;
}