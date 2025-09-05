package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_id")
    private Long id;
    private LocalDate date;
    @Column(nullable = true) // null es a particular
    private String saleNumber;
    private BigDecimal taxes; // discount

    @ManyToOne
    private Customer customer;
    
    private String remitoPath;
    
    @JsonManagedReference
    @OneToOne(optional = true)// null es NO facturado
    private Factura factura;
    
    @JsonManagedReference
    @OneToMany(mappedBy = "sale", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();
    
    //private char type;
    //state (completed, pending, canceled)
    @JsonIgnore
    public BigDecimal getTotal() {
    	BigDecimal total = BigDecimal.ZERO;
    	for(SaleDetail d : details)
    		total = total.add(d.getSubTotal());
    	return total.setScale(2, RoundingMode.HALF_UP);
    }
    
}