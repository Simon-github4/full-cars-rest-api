package com.fullcars.restapi.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fullcars.restapi.dto.ConfirmPurchasePayDTO;
import com.fullcars.restapi.model.Purchase;
import com.fullcars.restapi.service.PurchaseDetailService;
import com.fullcars.restapi.service.PurchaseService;

@RestController
@RequestMapping(value = "/purchases")
public class PurchaseController {

	private final PurchaseService purchaseService;
	private final PurchaseDetailService detailsService;

	public PurchaseController(PurchaseService repo, PurchaseDetailService repod) {
		this.purchaseService = repo;
		this.detailsService = repod;
	}
	
	@PostMapping("/{idProvider}")
	@ResponseStatus(HttpStatus.CREATED)
	public Purchase post(@RequestBody Purchase b, @PathVariable Long idProvider) {
		return purchaseService.save(b, idProvider);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Purchase getPurchase(@PathVariable Long id){
		return purchaseService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Purchase> getPurchases(){
		return purchaseService.getPurchases();
	}
	
	@GetMapping("/filters")
	@ResponseStatus(HttpStatus.OK)
	public List<Purchase> getPurchasesFiltered(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
		    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
		    @RequestParam(required = false) Long idProvider) {
		return purchaseService.getPurchases(start, end, idProvider);
	}	
	
	@DeleteMapping("/{id}")
	public void deletePurchase(@PathVariable Long id) {
		purchaseService.delete(id);
	}
	
	@PostMapping("/{id}/uploadBill")
	public ResponseEntity<?> uploadBill(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
	    try {
            String filePath = purchaseService.uploadBill(id, file);

            return ResponseEntity.ok("Archivo guardado en: " + filePath);
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar archivo");
	    }
	}

	@GetMapping("/{id}/getBill")
	public ResponseEntity<InputStreamResource> getBill(@PathVariable Long id) throws IOException {
	    File file = purchaseService.getBill(id);
	    
	    String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) 
            mimeType = "application/octet-stream";
        
        //InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .body(new InputStreamResource(new FileInputStream(file)));
	}

	@PatchMapping("/{id}/confirmPay")
	public void confirmPay(@PathVariable Long id, @RequestBody ConfirmPurchasePayDTO confirmPurchasePayDTO){
		purchaseService.confirmPay(id, confirmPurchasePayDTO.isPayed());
	}
/*----------------------------------------------- Details ----------------------------------------
	@PostMapping("/{id}/details")
	@ResponseStatus(HttpStatus.CREATED)
	public PurchaseDetail postPurchaseDetail(@RequestBody PurchaseDetail b) {
		return (PurchaseDetail)detailsService.save(b);
	}
	
	//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public PurchaseDetail put(@PathVariable Long id, @RequestBody PurchaseDetail b) {
		if (!id.equals(b.getId())) 
			throw new IllegalArgumentException("El ID enviado y el ID del detalle deben coincidir");
	        return detailsService.save(b);
	    }
		
	@GetMapping("/details/{id}")
	@ResponseStatus(HttpStatus.OK)
	public PurchaseDetail getPurchaseDetail(@PathVariable Long id){
		return detailsService.findByIdOrThrow(id);
	}
	
	/*@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<PurchaseDetail> getDetails(){
		return detailsService.getDetails();
	}
	@DeleteMapping("/details/{id}")
	public void deleteDetail(@PathVariable Long id) {
		detailsService.delete(id);
	}*/
	
}