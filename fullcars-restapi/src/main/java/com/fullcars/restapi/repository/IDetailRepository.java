package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Detail;
@Repository
public interface IDetailRepository extends JpaRepository<Detail, Long>{

}
