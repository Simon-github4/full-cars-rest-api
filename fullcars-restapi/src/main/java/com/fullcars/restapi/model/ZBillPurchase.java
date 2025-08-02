package com.fullcars.restapi.model;

import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
public class ZBillPurchase {

	@OneToOne
	private Purchase purchase;
	
	private String fileUrl;
	
}
