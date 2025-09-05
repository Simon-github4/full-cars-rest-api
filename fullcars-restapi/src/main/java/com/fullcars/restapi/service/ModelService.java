package com.fullcars.restapi.service;

import java.rmi.ServerException;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fullcars.restapi.model.Model;
import com.fullcars.restapi.repository.IModelRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ModelService {

	private IModelRepository modelRepo;
	
	public ModelService(IModelRepository repo) {
		this.modelRepo = repo;
	}
	
	@Transactional
	public Model save(Model b) throws ServerException {
		if(b.getId() == null && findByName(b.getName()) != null)
			throw new ServerException("Ya Existe un Modelo con ese nombre");
		return modelRepo.save(b);
	}
	@Transactional(readOnly = true)
	public Model findByName(String name) {
		return modelRepo.findByName(name);
	}
	
	@Transactional
	public void delete(Long id) {
        modelRepo.deleteById(id);
	}
	
	@Transactional(readOnly = true)
	public Model findByIdOrThrow(Long id) {
		return modelRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Modelo no encontrada con id: " + id));
	}
	@Transactional(readOnly = true)
	public List<Model> getModels(){
		return modelRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
}
