package com.fullcars.restapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class SaleDetail extends BaseDetail{

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id")
	private Sale sale;

}