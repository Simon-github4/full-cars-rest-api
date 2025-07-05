package com.fullcars.restapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.PurchaseDetail;
import com.fullcars.restapi.model.SaleDetail;
import com.fullcars.restapi.model.StockMovement;

@Repository
public interface IStockMovementRepository extends JpaRepository<StockMovement, Long>{

	public void deleteByPurchaseDetail(PurchaseDetail d);

	public void deleteBySaleDetail(SaleDetail d);
	
	public Optional<StockMovement> findByPurchaseDetail(PurchaseDetail d);

	public Optional<StockMovement> findBySaleDetail(SaleDetail d);
}
