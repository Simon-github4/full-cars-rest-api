package com.fullcars.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.fullcars.restapi.model.Model;

@Repository
public interface IModelRepository extends JpaRepository<Model, Long>{

		@Query("SELECT b FROM Model b WHERE lower(b.name) = lower(:name) ")
		Model findByName(String name);

}
