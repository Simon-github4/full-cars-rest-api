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
    private BigDecimal paymentAmount;
    private BigDecimal creditUsed;
    private LocalDate date;
    private String paymentMethod;
    
    private List<AllocationInfo> allocations;
    private List<SaleUpdate> salesUpdated;
    private CreditInfo creditGenerated;
    private BigDecimal customerCreditBalance;
    private String summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AllocationInfo {
        private Long saleId;
        private BigDecimal saleTotal;
        private BigDecimal amountApplied;
        private Boolean isCredit;
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreditInfo {
        private Long creditId;
        private BigDecimal amount;
        private String description;
    }
}
