package com.fullcars.restapi.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.ServerException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.PurchaseEvent;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.repository.IPurchaseRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PurchaseService {

	private final IPurchaseRepository purchaseRepo;
	private final ProviderService providerService;
	private final ApplicationEventPublisher appEventPublisher;

	public PurchaseService(IPurchaseRepository repo, ProviderService providerService, ApplicationEventPublisher appEventPublisher) {
		this.purchaseRepo = repo;
		this.providerService = providerService;
		this.appEventPublisher = appEventPublisher;
	}
	
	@Transactional
	public Purchase save(Purchase p, Long idProvider) {
		p.setProvider(providerService.findByIdOrThrow(idProvider));
		p.getDetails().forEach(d -> d.setPurchase(p));
		Purchase savedPurchase = purchaseRepo.save(p);
		appEventPublisher.publishEvent(new PurchaseEvent(this, savedPurchase, EventType.INSERT));
		return savedPurchase;
	}
	
	@Transactional
	public void delete(Long id) {
        Purchase deletedPurchase = purchaseRepo.findById(id).orElseThrow(()-> 
        							new EntityNotFoundException("Compra no encontrado con id: " + id)); 
        purchaseRepo.deleteById(id);
        String filePath = deletedPurchase.getFilePath();
        appEventPublisher.publishEvent(new PurchaseEvent(this, deletedPurchase, EventType.DELETE));
        if(filePath != null && !filePath.isBlank())
			try {
				Files.deleteIfExists(Paths.get(filePath));
			} catch (IOException e) {
				System.err.println("No se pudo borrar el archivo de la compra");
				e.printStackTrace();
			}
	}
	
    @Transactional(readOnly = true)
	public Purchase findByIdOrThrow(Long id) {
		return purchaseRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Compra no encontrada con id: " + id));
	}
	
    @Transactional(readOnly = true)
	public List<Purchase> getPurchases(){
		return purchaseRepo.findAll();
	}

	@Transactional(readOnly = true)
	public List<Purchase> getPurchases(LocalDate start, LocalDate end, Long idProvider) {
		if (start != null && end != null && idProvider != null) 
	        return purchaseRepo.findByDateBetweenAndProviderId(start, end, idProvider);
	     else if (start != null && end != null) 
	        return purchaseRepo.findByDateBetween(start, end);
	     else if (idProvider != null) 
	        return purchaseRepo.findByProviderId(idProvider);
	     else 
	        return purchaseRepo.findAll();
	}

	public String uploadBill(Long id, MultipartFile file) throws IOException {
		String desktopPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Archivos de Compras";
    	Path folderPath = Paths.get(desktopPath);
        if (!Files.exists(folderPath)) 
            Files.createDirectories(folderPath);

        String filePrefix = "Compra Nro " + id.toString();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, filePrefix + ".*")) {
            for (Path existingFile : stream) 
                Files.deleteIfExists(existingFile);
        }

        String fileName =  filePrefix + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));//with extension
        Path filePath = folderPath.resolve(fileName);
        
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
		
		purchaseRepo.updateFilePathById(id, filePath.toString());
		return fileName;
	}

	public File getBill(Long id) throws ServerException {
		Path filePath = Paths.get(purchaseRepo.findPurchaseFilePath(id));
		
		if (filePath == null) 
	        throw new ServerException("No hay archivo registrado para esta compra");

	    File file = (filePath.toFile());
	    if (!file.exists()) 
	    	throw new ServerException("El archivo no existe en el servidor");

		return file;
	}

	
	public void confirmPay(Long id, boolean payed) {
		purchaseRepo.updateIsPayed(id, payed);
	}

	public List<Long> getPurchasesIdNotPayed() {
		return purchaseRepo.findByIsPayed(false);
	}
	
}
