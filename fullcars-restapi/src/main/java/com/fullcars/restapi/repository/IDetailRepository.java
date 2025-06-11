package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Detail;

public interface IDetailRepository extends JpaRepository<Detail, Long>{

}
