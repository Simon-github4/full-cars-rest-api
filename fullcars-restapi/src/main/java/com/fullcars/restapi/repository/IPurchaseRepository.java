package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Purchase;

@Repository
public interface IPurchaseRepository extends JpaRepository<Purchase, Long>{

}
