package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Pay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    
    private LocalDate date;
    
    private String description;
    
    @Column(name = "credit_used", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal creditUsed = BigDecimal.ZERO;
    
    @Column(name = "credit_generated", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal creditGenerated = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "pay", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentSplit> splits = new ArrayList<>();

    public void addSplit(PaymentSplit split) {
        splits.add(split);
        split.setPay(this);
    }
    
    public BigDecimal getTotalAmount() {
        return splits.stream()
                .map(PaymentSplit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}