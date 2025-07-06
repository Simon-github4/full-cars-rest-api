package com.fullcars.restapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
	
	@Query("""
		    SELECT 
		        COALESCE(SUM(
		            CASE 
		                WHEN m.tipo IN ('ENTRADA_COMPRA', 'ENTRADA_AJUSTE') THEN m.cantidad
		                WHEN m.tipo IN ('SALIDA_VENTA', 'SALIDA_AJUSTE') THEN -m.cantidad
		                ELSE 0
		            END
		        ), 0)
		    FROM MovimientoStock m
		    WHERE m.parte.id = :carPartId
		""")
	public Long getCurrentStockByCarPartId(@Param("carPartId") Long carPartId);

}
