package com.fullcars.restapi.model;

import java.time.LocalDate;

import com.fullcars.restapi.enums.MovementType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;
    private LocalDate date;
    private String reference;//EJ: Sale #155 OR Detail  ??
    //private String observations;
    @ManyToOne
    private CarPart carPart;

    @Enumerated(EnumType.STRING)
    private MovementType type;
    
    @OneToOne(optional = true)
    private SaleDetail saleDetail;
    @OneToOne(optional = true)
    private PurchaseDetail purchaseDetail;
    //Uno de los dos va a ser null, TODO A fututo analizar sacar mappedsuperclass
    
}