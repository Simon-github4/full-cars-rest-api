package com.fullcars.restapi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fullcars.restapi.enums.MovementType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
@Builder
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer quantity;
    private LocalDate date;
    private String reference;//EJ: Sale #155 OR Detail  ??
    private String observations;
    
    @ManyToOne
    private CarPart carPart;

    @Enumerated(EnumType.STRING)
    private MovementType type;

}