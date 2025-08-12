package com.fullcars.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Pay;
@Repository
public interface IPayRepository extends JpaRepository<Pay, Long>{

	List<Pay> findByCustomer(Long customerId);
}
