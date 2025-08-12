package com.fullcars.restapi.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.model.Sale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDTO {

    private Customer customer;
    private List<Sale> sales;
    private List<Pay> payments;
    //private BigDecimal saldo;
    
}
