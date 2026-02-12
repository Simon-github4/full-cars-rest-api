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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ISaleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SaleService {

	private final ISaleRepository saleRepo;
	private final CustomerService customerService;
	private final ApplicationEventPublisher appEventPublisher;
	private final EmailService mailService;
	private static final String REMITOS_PATH = "C:"+ File.separator + "SoftwareFullCars"+ File.separator + "Remitos de Ventas";
	private static final String COMODIN_SKU = "COMODIN";

	public SaleService(ISaleRepository repo, ApplicationEventPublisher publisher, CustomerService customerService, EmailService mailService) {
		this.saleRepo = repo;
		this.appEventPublisher = publisher;
		this.customerService = customerService;
		this.mailService = mailService;
	}
	
	@Transactional
	public Sale save(Sale sale, Long idCustomer) {
		sale.setCustomer(customerService.findByIdOrThrow(idCustomer));
		sale.getDetails().forEach(d -> {
			d.setSale(sale);
			if(d.getCarPart().getSku().equalsIgnoreCase(COMODIN_SKU)) {
		        if (d.getPrintedDescription() == null || d.getPrintedDescription().trim().isEmpty()) 
		            throw new IllegalArgumentException("El artículo "+COMODIN_SKU+" requiere una descripción manual obligatoria.");
		    }else 
		        d.setPrintedDescription(null); //Si NO es comodín, forzamos NULL para no guardar basura (siguiendo tu lógica)
		});
		
		Sale savedSale = saleRepo.save(sale);
		appEventPublisher.publishEvent(new SaleEvent(this, savedSale, EventType.INSERT));
		return savedSale;
	}
	
	@Transactional
	public Sale update(Sale sale) {
		sale.getDetails().forEach(d -> d.setSale(sale));
		
		return saleRepo.save(sale);
	}

	@Transactional
	public void delete(Long id) {
		Sale sale = saleRepo.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Venta no encontrada con id: " + id));

		if (sale.getFactura() != null) {

			if (sale.isAnulada()) 
				throw new RuntimeException("La venta ya se encuentra anulada.");
			
			sale.setAnulada(true);
			saleRepo.save(sale);
			appEventPublisher.publishEvent(new SaleEvent(this, sale, EventType.ANULACION));

		}else {
			appEventPublisher.publishEvent(new SaleEvent(this, sale, EventType.DELETE));

			String filePath = sale.getRemitoPath();
			if (filePath != null) 
				try {
					Files.deleteIfExists(Paths.get(filePath));
				} catch (IOException e) {
					System.err.println("No se pudo borrar el remito de venta: " + filePath);
					e.printStackTrace();
				}

			saleRepo.deleteById(id);//arriba de borrar file antes
		}
        
	}
	
	@Transactional(readOnly = true)
	public Sale findByIdOrThrow(Long id) {
		return saleRepo.findById(id).orElseThrow(() -> 
						new EntityNotFoundException("Venta no encontrada con id: " + id));
	}
	
	@Transactional(readOnly = true)
	public List<Sale> getSales(){
		return saleRepo.findAll();
	}

	@Transactional(readOnly = true)
	public List<Sale> getSales(LocalDate start, LocalDate end, Long idCustomer) {
		if (start != null && end != null && idCustomer != null) {
	        return saleRepo.findByDateBetweenAndCustomerId(start, end, idCustomer);
	    } else if (start != null && end != null) {
	        return saleRepo.findByDateBetween(start, end);
	    } else if (idCustomer != null) {
	        return saleRepo.findByCustomerId(idCustomer);
	    } else {
	        return saleRepo.findAll();
	    }
	}

	public String uploadRemito(Long id, MultipartFile file) throws Exception {//it is called after new sale save on controller
		//String desktopPath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Remitos de Ventas";

		Path folderPath = Paths.get(REMITOS_PATH);
        if (!Files.exists(folderPath)) 
            Files.createDirectories(folderPath);

        String filePrefix = "Remito de Venta Nro " + id.toString();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath, filePrefix + ".*")) {
            for (Path existingFile : stream) 
                Files.deleteIfExists(existingFile);
        }

        String fileName = filePrefix + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));//with extension
        Path filePath = folderPath.resolve(fileName);
        
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            mailService.sendEmail(saleRepo.findEmailById(id), "Remito",
                    			"",//"<h1>Se generó una venta</h1><p>ID: " + id + "</p>", 
                    			filePath.toFile());
        }
		
		saleRepo.updateRemitoPathById(id, filePath.toString());
		return fileName;
	}

	public File getRemito(Long saleId) throws ServerException {
		Path filePath = Paths.get(saleRepo.findRemitoPathById(saleId));
		
		if (filePath == null) 
	        throw new ServerException("No hay Remito registrado para esta venta");

	    File file = filePath.toFile();
	    if (!file.exists()) 
	    	throw new ServerException("El Remito no existe en el servidor");

		return file;
	}

	public List<Sale> getRecentSales(int limit) {
	    return saleRepo.findRecentSales(PageRequest.of(0, limit));
	}
	
}
