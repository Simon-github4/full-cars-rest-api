package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fullcars.restapi.model.Category;

public interface ICategoryRepository extends JpaRepository<Category, Long>{

}
