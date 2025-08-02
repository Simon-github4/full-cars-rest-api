package com.fullcars.restapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Sale;

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

}
