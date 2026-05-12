package com.fullcars.restapi.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSplitDTO {

    private Long splitId;
    private BigDecimal amount;
    private String paymentMethod;
    private String reference;
    private List<String> salesCovered;
}