package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale")
@Data
@NoArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sale_id")
    private Long id;
    private LocalDate date;
    private String saleNumber;
    private BigDecimal taxes;
    //private String factura / remito Url;
    //en vez de poner customer, plasmar los atributos de el customer en ESE MOMENTO
    @ManyToOne
    private Customer customer;
    
    @OneToMany(mappedBy = "sale", fetch = FetchType.EAGER) //cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<SaleDetail> details;
    
    
}