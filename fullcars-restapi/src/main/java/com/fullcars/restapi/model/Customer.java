package com.fullcars.restapi.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal creditBalance = BigDecimal.ZERO;
    
    private String fullName;
    private String dni;
    private String cuit;
    private String email;
    private String phone;
    private String adress;

}