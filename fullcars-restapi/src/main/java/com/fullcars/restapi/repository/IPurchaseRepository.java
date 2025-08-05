package com.fullcars.restapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Purchase;

@Repository
public interface IPurchaseRepository extends JpaRepository<Purchase, Long>{

	List<Purchase> findByDateBetweenAndProviderId(LocalDate start, LocalDate end, Long providerId);
	List<Purchase> findByDateBetween(LocalDate start, LocalDate end);
	List<Purchase> findByProviderId(Long providerId);
}
