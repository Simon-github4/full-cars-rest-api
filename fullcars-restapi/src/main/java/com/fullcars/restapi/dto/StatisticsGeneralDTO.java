package com.fullcars.restapi.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.model.Sale;

import lombok.Getter;

@Getter
public class StatisticsGeneralDTO {
	
	private long itemsRegistered;
	private BigDecimal totalToCharge;
	private List<SalesData> salesData;
	private List<Purchase> purchases;
	private List<Sale> recentSales;
	private List<TopProductDTO> topProducts;
	private List<CarPart> criticalStock;
	
	private List<Long> purchasesNotPayed;
	//private List<String> notifications;
	//private List<String> metricValues; // total ventas en el rango, compras total, ...

	public StatisticsGeneralDTO() {
		// TODO Auto-generated constructor stub
	}

	public StatisticsGeneralDTO(long itemsRegistered, BigDecimal totalToCharge, List<SalesData> salesData,
			List<Purchase> purchases, List<Sale> recentSales, List<TopProductDTO> topProducts, List<CarPart> criticalStock,
			List<Long> purchasesNotPayed) {
		super();
		this.itemsRegistered = itemsRegistered;
		this.totalToCharge = totalToCharge;
		this.salesData = salesData;
		this.purchases = purchases;
		this.recentSales = recentSales;
		this.topProducts = topProducts;
		this.criticalStock = criticalStock;
		this.purchasesNotPayed = purchasesNotPayed;
	}


}