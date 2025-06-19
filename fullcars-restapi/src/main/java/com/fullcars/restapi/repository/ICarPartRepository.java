package com.fullcars.restapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.CarPart;
@Repository
public interface ICarPartRepository extends JpaRepository<CarPart, Long>{

    Optional<CarPart> findBySku(String sku);

}
