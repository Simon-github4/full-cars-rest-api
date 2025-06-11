package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Purchase;

public interface IPurchaseRepository extends JpaRepository<Purchase, Long>{

}
