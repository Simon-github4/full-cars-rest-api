package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.SaleDetail;

@Repository
public interface ISaleDetailRepository extends JpaRepository<SaleDetail, Long>{

}
