package com.fullcars.restapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiPaymentResponse {

    private Long paymentId;
    private Long customerId;
    private LocalDate date;
    private String description;
    
    private BigDecimal totalAmount;
    private BigDecimal creditUsed;
    private BigDecimal creditGenerated;
    private BigDecimal customerCreditBalance;
    
    private List<PaymentSplitDTO> splits;
    private String summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaleUpdate {
        private Long saleId;
        private BigDecimal total;
        private BigDecimal totalPaid;
        private BigDecimal remainingDue;
        private boolean paid;
    }
}