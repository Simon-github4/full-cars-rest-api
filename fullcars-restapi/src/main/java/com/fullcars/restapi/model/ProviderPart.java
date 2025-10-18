package com.fullcars.restapi.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider_part")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderPart {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "provider_part_seq")
	@SequenceGenerator(name = "provider_part_seq", sequenceName = "provider_part_seq", allocationSize = 500)
	private Long id;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderMapping providerMapping;

    private String nombre;
    private String marca;
    private BigDecimal precio;

	private String provCod;
	//private String quality;
	private String category;
}
