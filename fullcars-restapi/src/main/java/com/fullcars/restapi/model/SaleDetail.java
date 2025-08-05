package com.fullcars.restapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
//@Table(name = "sale_details")
public class SaleDetail extends BaseDetail{
	
    @JsonBackReference
	@ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "sale_id")
	private Sale sale;

	//@Override
	//public String getTableName() {
	//	return "sale_details";
	//}

}