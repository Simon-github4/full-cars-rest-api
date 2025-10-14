package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fullcars.restapi.model.Brand;
import com.fullcars.restapi.model.Category;

@Repository
public interface ICategoryRepository extends JpaRepository<Category, Long>{

	Category findByName(String name);

}
