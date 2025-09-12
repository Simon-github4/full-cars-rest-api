package com.fullcars.restapi.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ProviderService {

	private IProviderRepository providerRepo;
	private IProviderMappingRepository mappingRepo;
	private IProviderPartRepository providerPartsRepo;
	
	public ProviderService(IProviderRepository providerRepo, IProviderMappingRepository mappingRepo, IProviderPartRepository providerPartsRepo) {
		this.providerRepo = providerRepo;
		this.mappingRepo = mappingRepo;
		this.providerPartsRepo = providerPartsRepo;
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
	public ProviderMapping uploadMapping(ProviderMapping mapping, MultipartFile archivoExcel) throws IOException, Exception {
        ProviderMapping existingMapping = mappingRepo.findByProviderId(mapping.getProviderId())
                .orElse(mapping);
        existingMapping.setNameColumn(mapping.getNameColumn());
        existingMapping.setBrandColumn(mapping.getBrandColumn());
        existingMapping.setPriceColumn(mapping.getPriceColumn());

        existingMapping.setLastUpdate(LocalDateTime.now());
        ProviderMapping savedMapping = mappingRepo.save(existingMapping);

        //String extension = archivoExcel.getOriginalFilename().substring(archivoExcel.getOriginalFilename().lastIndexOf('.') + 1).toLowerCase();
        List<ProviderPart> partes = FileMapper.mapFile(archivoExcel.getInputStream(), savedMapping, archivoExcel.getOriginalFilename());
        
        providerPartsRepo.deleteByProviderMapping(savedMapping);
        providerPartsRepo.saveAll(partes);
        
        return savedMapping;
	}

	public ProviderMapping findProviderMapping(Long providerId) {
		return mappingRepo.findByProviderId(providerId).orElseThrow(() -> 
				new EntityNotFoundException("Mapeo Proveedor no encontrada con id proveedor: " + providerId));
	}
}
