package com.fullcars.restapi.dto;

import java.time.LocalDate;

public class SalesData {
    private final long amount;
    private final LocalDate date;

    public SalesData(long amount, LocalDate date) {
        this.amount = amount;
        this.date = date;
    }

    public long getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
