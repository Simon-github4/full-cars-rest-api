package com.fullcars.restapi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "sale_id")
    private Long id;
    private LocalDate date;
    private String saleNumber;
    private BigDecimal taxes; // discount

    @ManyToOne
    private Customer customer;
    /* deberian estar en SaleBill 
     	private String adressSnapshot;
    	private String cuitSnapshot;	
    	private String fullNameSnapshot;
     */
    @JsonManagedReference
    @OneToMany(mappedBy = "sale", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<SaleDetail> details = new ArrayList<>();
    
    //private String factura / remito Url;
    //private char type;
    //state (completed, pending, canceled)
}