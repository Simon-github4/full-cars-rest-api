package com.fullcars.restapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesData {
    private final BigDecimal amount;
    private final LocalDate date;

    public SalesData(BigDecimal amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }

    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
