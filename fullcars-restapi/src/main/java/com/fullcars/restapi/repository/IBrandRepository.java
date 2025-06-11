package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Brand;

@Repository
public interface IBrandRepository extends JpaRepository<Brand, Long>{

}
