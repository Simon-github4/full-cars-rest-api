package com.fullcars.restapi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;

import jakarta.transaction.Transactional;

@Repository
public interface ISaleRepository extends JpaRepository<Sale, Long>{
	/*
	@Query("""
	        SELECT s FROM Sale s
	        WHERE (:start IS NULL OR s.date >= :start)
	          AND (:end IS NULL OR s.date <= :end)
	          AND (:customerId IS NULL OR s.customer.id = :customerId)
	        ORDER BY s.date DESC
	    """)
	    List<Sale> findByDateBetweenAndCustomerId(
	        @Param("start") LocalDate start,
	        @Param("end") LocalDate end,
	        @Param("customerId") Long customerId
	    );*/
	List<Sale> findByDateBetweenAndCustomerId(LocalDate start, LocalDate end, Long customerId);
	List<Sale> findByDateBetween(LocalDate start, LocalDate end);
	List<Sale> findByCustomerId(Long customerId);
	@Modifying
	@Transactional
	@Query("UPDATE Sale s SET s.remitoPath = :filePath WHERE s.id = :id")
	void updateRemitoPathById(Long id, String filePath);
	
	@Query("SELECT s FROM Sale s ORDER BY s.date DESC")
    List<Sale> findRecentSales(Pageable pageable);
	
	@Query("SELECT s.remitoPath FROM Sale s WHERE s.id = :id")
	String findRemitoPathById(@Param("id") Long id);

	@Query("SELECT s.customer.email FROM Sale s WHERE s.id = :id")
	String findEmailById(@Param("id") Long id);
	
	@Query(value = "SELECT s FROM sale WHERE s.anulada = true", nativeQuery = true)
    List<Sale> buscarSoloAnuladas();

    @Query("SELECT s FROM Sale s WHERE s.customer.id = :customerId AND s.anulada = false ORDER BY s.date DESC")
    List<Sale> findPendingByCustomerId(@Param("customerId") Long customerId);

    @Query("""
        SELECT s FROM Sale s 
        WHERE s.customer.id = :customerId 
        AND s.anulada = false 
        ORDER BY s.date ASC
        """)
    List<Sale> findByCustomerIdOrderByDate(@Param("customerId") Long customerId);

    Optional<Sale> findByFacturaId(Long facturaId);

    @Query("SELECT s.factura FROM Sale s WHERE s.id = :saleId")
    Optional<Factura> findFacturaBySaleId(@Param("saleId") Long saleId);
}
