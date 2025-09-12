package com.fullcars.restapi.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "provider_mapping")
public class ProviderMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonIgnore
	private Long id;
	@Column(name = "provider_id", nullable = false, unique = true)//No guardo la entidad para evitar joins innecesarios
	private Long providerId;

	private String nameColumn;
	private String brandColumn;
	private String priceColumn;

	private LocalDateTime lastUpdate;
    
}
