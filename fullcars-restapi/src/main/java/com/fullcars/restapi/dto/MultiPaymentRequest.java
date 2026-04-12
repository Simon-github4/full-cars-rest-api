package com.fullcars.restapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiPaymentRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long customerId;

    @NotNull(message = "El monto del pago es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal paymentAmount;

    private String paymentMethod;

    private LocalDate date;

    private String notes;

    private List<Long> saleIds;
    
    private Boolean useCredit;
}
