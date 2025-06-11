package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Pay;

public interface IPayRepository extends JpaRepository<Pay, Long>{

}
