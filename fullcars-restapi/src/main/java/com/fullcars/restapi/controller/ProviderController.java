package com.fullcars.restapi.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fullcars.restapi.dto.ProviderPartDTO;
import com.fullcars.restapi.model.Provider;
import com.fullcars.restapi.model.ProviderMapping;
import com.fullcars.restapi.model.ProviderPart;
import com.fullcars.restapi.service.ProviderService;
import com.fullcars.restapi.service.excel.TaskQueueService;

import jakarta.persistence.EntityManager;

@RestController
@RequestMapping(value = "/providers")
public class ProviderController {

	private final ProviderService providerService;
	private final TaskQueueService taskService;
	@Autowired
	private EntityManager em;
	public ProviderController(ProviderService repo, TaskQueueService taskService) {
        this.taskService = taskService;
        this.providerService = repo;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Provider post(@RequestBody Provider b) {
		return providerService.save(b);
	}
//----------------PODRIAN SER EL MISMO, USAN EL MISMO METODO SAVE-------------------------
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Provider put(@PathVariable Long id, @RequestBody Provider b) {
		if (!id.equals(b.getId())) 
            throw new IllegalArgumentException("El ID enviado y el ID de la categoria deben coincidir");
        return providerService.save(b);
    }
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Provider getProvider(@PathVariable Long id){
		return providerService.findByIdOrThrow(id);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<Provider> getProviders(){
		return providerService.getCategories();
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		providerService.delete(id);
	}

	@GetMapping("/parts")
	@ResponseStatus(HttpStatus.OK)
	public List<ProviderPartDTO> getProviderParts() {
		//Thread t = new Thread(()->{
		//	System.gc();
		//});
		//t.start();
		return providerService.getProviderPartsDTO();
	    // em.clear();
	     /*.stream() 
	        .map(p -> new ProviderPartDTO(
	            p.getId(),
	            p.getNombre(),
	            p.getMarca(),
	            p.getPrecio(),
	            p.getProviderMapping().getProviderId()
	        ))
	        .toList();*/
	}

	
	@GetMapping("/{providerId}/mapping")
	@ResponseStatus(HttpStatus.OK)
	public ProviderMapping getProviderMapping(@PathVariable Long providerId){
		return providerService.findProviderMapping(providerId);
	}
	
	@PostMapping("/{providerId}/mapping")
    public ResponseEntity<?> uploadMapping(
            @PathVariable Long providerId,
            @RequestPart("mapping") ProviderMapping mapping,
            @RequestPart("archivoExcel") MultipartFile archivoExcel
    ) {
        try {
        	if(providerId == null || providerId < 1)
        		return ResponseEntity.status(500).body("ID Proveedor Invalido");
        	
        	mapping.setProviderId(providerId);
            return ResponseEntity.accepted().body(providerService.uploadMapping(mapping, archivoExcel));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al procesar archivo: " + e.getMessage());
        }
    }
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskQueueService.TaskStatusInfo> getTaskStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(taskService.getStatus(taskId));
    }
	
}