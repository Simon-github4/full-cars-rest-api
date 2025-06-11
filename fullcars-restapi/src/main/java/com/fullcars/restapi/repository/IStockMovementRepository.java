package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.StockMovement;

public interface IStockMovementRepository extends JpaRepository<StockMovement, Long>{

}
