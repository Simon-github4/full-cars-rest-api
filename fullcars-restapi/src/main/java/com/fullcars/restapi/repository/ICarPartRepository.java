package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.CarPart;

public interface ICarPartRepository extends JpaRepository<CarPart, Long>{

}
