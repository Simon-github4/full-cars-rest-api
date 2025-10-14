package com.fullcars.restapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Purchase;

import jakarta.transaction.Transactional;

@Repository
public interface IPurchaseRepository extends JpaRepository<Purchase, Long>{

	List<Purchase> findByDateBetweenAndProviderId(LocalDate start, LocalDate end, Long providerId);
	List<Purchase> findByDateBetween(LocalDate start, LocalDate end);
	List<Purchase> findByProviderId(Long providerId);	

	@Modifying
	@Transactional
	@Query("UPDATE Purchase p SET p.filePath = :filePath WHERE p.id = :id")
	void updateFilePathById(Long id, String filePath);	
	
	@Query("SELECT p.filePath FROM Purchase p WHERE p.id = :id")
	String findPurchaseFilePath(@Param("id") Long id);
	
	@Modifying
	@Transactional
	@Query("UPDATE Purchase p SET p.isPayed = :payed WHERE p.id = :id")
	void updateIsPayed(Long id, boolean payed);
	
	@Query("SELECT p.id FROM Purchase p WHERE p.isPayed = :isPayed")
	List<Long> findByIsPayed(boolean isPayed);
	
	@Query("SELECT p FROM Purchase p WHERE LOWER(p.facturaNumber) LIKE LOWER(CONCAT(:facturaNumber, '%'))")
	List<Purchase> findByFacturaNumberStartingWith(String facturaNumber);


}
