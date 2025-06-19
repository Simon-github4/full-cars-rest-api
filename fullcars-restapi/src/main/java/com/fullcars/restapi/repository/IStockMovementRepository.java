package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.StockMovement;

@Repository
public interface IStockMovementRepository extends JpaRepository<StockMovement, Long>{

}
