package com.fullcars.restapi.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.model.SaleDetail;
import com.fullcars.restapi.service.SaleDetailService;
import com.fullcars.restapi.service.SaleService;

@RestController
@RequestMapping(value = "/sales")
public class SaleController {

private final SaleService saleService;
private final SaleDetailService detailsService;
	
	public SaleController(SaleService repo, SaleDetailService repod) {
		this.saleService = repo;
		this.detailsService = repod;
	}
	
	@PostMapping("/{idCustomer}")
	@ResponseStatus(HttpStatus.CREATED)
	public Sale post(@RequestBody Sale sale, @PathVariable Long idCustomer) {
		return saleService.save(sale, idCustomer);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Sale getSale(@PathVariable Long id){
		return saleService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Sale> getSales(){
		return saleService.getSales();
	}
	
	@GetMapping("/filters")
	@ResponseStatus(HttpStatus.OK)
	public List<Sale> getSalesFiltered(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
		    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
		    @RequestParam(required = false) Long idCustomer) {
		return saleService.getSales(start, end, idCustomer);
	}	
	
	@DeleteMapping("/{id}")
	public void deleteSale(@PathVariable Long id) {
		saleService.delete(id);
	}
	
	@PostMapping("/{id}/uploadRemito")
	public ResponseEntity<?> uploadRemito(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
	    try {
            String filePath = saleService.uploadRemito(id, file);

            return ResponseEntity.ok("Archivo guardado en: " + filePath);
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar archivo");
	    }
	}
	
	@GetMapping("/{id}/getRemito")
	public ResponseEntity<InputStreamResource> getRemito(@PathVariable Long id) throws IOException {
	    File file = saleService.getRemito(id);
	    
	    String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) 
            mimeType = "application/octet-stream";
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .body(new InputStreamResource(new FileInputStream(file)));
	}
/*------------------------------------------- Details ------------------------------------------------------
	@PostMapping("/{id}/details")
	@ResponseStatus(HttpStatus.CREATED)
	public SaleDetail postSaleDetail(@PathVariable Long id, @RequestBody SaleDetail b) {
		return (SaleDetail) detailsService.save(b);
	}

	@PutMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public SaleDetail put(@PathVariable Long id, @RequestBody SaleDetail b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID del detalle deben coincidir");
        return detailsService.save(b);
    }
	
	@GetMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public SaleDetail getSaleDetail(@PathVariable Long id){
		return detailsService.findByIdOrThrow(id);
	}

	@DeleteMapping("/details/{id}")
	public void deleteDetail(@PathVariable Long id) {
		detailsService.delete(id);
	}
	
	/*@GetMapping("/{id}/details")   innecesario, getSales los devuelve
	@ResponseStatus(HttpStatus.OK)
	public List<SaleDetail> getDetails(){
		return detailsService.getDetails();
	}*/
	
	
}
