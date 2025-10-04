package com.fullcars.restapi.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import com.fullcars.restapi.event.PurchaseEvent;
import com.fullcars.restapi.model.Provider;
import com.fullcars.restapi.model.ProviderMapping;
import com.fullcars.restapi.model.ProviderPart;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.repository.IProviderMappingRepository;
import com.fullcars.restapi.repository.IProviderPartRepository;
import com.fullcars.restapi.repository.IProviderRepository;
import com.fullcars.restapi.service.excel.ProviderExcelProcessor;
import com.fullcars.restapi.service.excel.TaskQueueService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ProviderService {

	private IProviderRepository providerRepo;
	private IProviderMappingRepository mappingRepo;
	private IProviderPartRepository providerPartsRepo;
	private final TaskQueueService taskService;
    private ProviderExcelProcessor excelProcessor;
    
	public ProviderService(IProviderRepository providerRepo, IProviderMappingRepository mappingRepo,
			IProviderPartRepository providerPartsRepo, TaskQueueService taskService, ProviderExcelProcessor excelProcessor) {
		this.taskService = taskService;
		this.providerRepo = providerRepo;
		this.mappingRepo = mappingRepo;
		this.providerPartsRepo = providerPartsRepo;
		this.excelProcessor = excelProcessor;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onApplicationEvent(PurchaseEvent e) {
		Purchase sale = e.getEntity();
		System.err.println("PurchaseEvent REceived!!!; ProviderService" + e.getSource());
	}
	
	public Provider save(Provider c) {
		return providerRepo.save(c);
	}
	
	public void delete(Long id) {
        providerRepo.deleteById(id);
	}
	
	public Provider findByIdOrThrow(Long id) {
		return providerRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Proveedor no encontrada con id: " + id));
	}
	
	public List<Provider> getCategories(){
		return providerRepo.findAll(Sort.by(Sort.Direction.ASC, "companyName"));
	}

	@Transactional
	public String uploadMapping(ProviderMapping mapping, MultipartFile archivoExcel) throws IOException {
	    ProviderMapping existingMapping = mappingRepo.findByProviderId(mapping.getProviderId())
	            .orElse(mapping);
	    existingMapping.setNameColumn(mapping.getNameColumn());
	    existingMapping.setBrandColumn(mapping.getBrandColumn());
	    existingMapping.setPriceColumn(mapping.getPriceColumn());
	    existingMapping.setLastUpdate(LocalDateTime.now());
	    ProviderMapping savedMapping = mappingRepo.save(existingMapping);

	    File tempFile = File.createTempFile("upload_"+mapping.getId(), 
	    		archivoExcel.getOriginalFilename().substring(archivoExcel.getOriginalFilename().lastIndexOf('.')));
	    archivoExcel.transferTo(tempFile);

	    // Encolar la tarea pasando el File
	    String taskId = taskService.enqueue(() -> {
	    	try {
				excelProcessor.processExcel(tempFile, savedMapping);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
	    });

	    return taskId; 
	}
	
	public ProviderMapping findProviderMapping(Long providerId) {
		return mappingRepo.findByProviderId(providerId).orElseThrow(() -> 
				new EntityNotFoundException("Mapeo Proveedor no encontrada con id proveedor: " + providerId));
	}

	public List<ProviderPart> getProviderParts() {
		return providerPartsRepo.findAll();
	}
}
