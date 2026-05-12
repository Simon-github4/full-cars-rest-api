package com.fullcars.restapi.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotEmpty(message = "Debe haber al menos un metodo de pago")
    @Valid
    private List<PaymentSplitRequest> splits;
    
    private LocalDate date;

    private String notes;

    private List<Long> saleIds;
    
    private Boolean useCredit;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSplitRequest {
        @NotNull(message = "El monto es obligatorio")
        private java.math.BigDecimal amount;
        
        @NotNull(message = "El metodo de pago es obligatorio")
        private String paymentMethod;
        
        private String reference;
    }
}