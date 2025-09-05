package com.fullcars.restapi.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fullcars.restapi.dto.SalesData;
import com.fullcars.restapi.dto.StatisticsGeneralDTO;
import com.fullcars.restapi.dto.TopProductDTO;
import com.fullcars.restapi.model.CarPart;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.service.CarPartService;
import com.fullcars.restapi.service.CustomerService;
import com.fullcars.restapi.service.PurchaseService;
import com.fullcars.restapi.service.SaleService;

@RestController
@RequestMapping(value = "/statistics")
public class StatisticsController {

    private final SaleService saleService;
    private final PurchaseService purchaseService;
    private final CarPartService carPartService;
    private final CustomerService customerService;

    @Autowired
    public StatisticsController(SaleService saleService, CarPartService carPartService, PurchaseService purchaseService, CustomerService customerService) {
        this.saleService = saleService;
        this.carPartService = carPartService;
        this.purchaseService = purchaseService;
        this.customerService = customerService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public StatisticsGeneralDTO getStatisticsDTO(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        long itemsRegistered = carPartService.getRegisteredCarParts();
        BigDecimal totalToCharge = customerService.calculateTotalToCharge();
        List<SalesData> salesDataList = saleService.getSales(start, end, null)
                .stream()
                .map(sale -> new SalesData(sale.getTotal(), sale.getDate()))
                .collect(Collectors.toList());

    	List<Purchase> purchases = purchaseService.getPurchases(start, end, null);
    	List<Long> purchasesNotPayed = purchaseService.getPurchasesIdNotPayed(); //= notificationService.getNotifications();
    	
    	List<CarPart> criticalStock = carPartService.getCriticalStock();
    	List<TopProductDTO> topProducts = carPartService.getTopProducts(10);
    	List<Sale> recentSales = saleService.getRecentSales(10);

        return new StatisticsGeneralDTO(
                itemsRegistered,
                totalToCharge,
                salesDataList,
                purchases,
                recentSales,
                topProducts,
                criticalStock,
                purchasesNotPayed
        );
    }
}

