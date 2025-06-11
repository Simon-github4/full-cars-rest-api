package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Sale;

public interface ISaleRepository extends JpaRepository<Sale, Long>{

}
