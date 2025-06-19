package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Sale;

@Repository
public interface ISaleRepository extends JpaRepository<Sale, Long>{

}
