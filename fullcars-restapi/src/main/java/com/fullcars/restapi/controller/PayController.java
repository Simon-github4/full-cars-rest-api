package com.fullcars.restapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fullcars.restapi.dto.MultiPaymentRequest;
import com.fullcars.restapi.dto.MultiPaymentResponse;
import com.fullcars.restapi.dto.PendingSalesResponse;
import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.service.MultiPaymentService;
import com.fullcars.restapi.service.PayService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/payments")
public class PayController {

//    private final PayService payService;
    private final MultiPaymentService multiPaymentService;

    public PayController(MultiPaymentService multiPaymentService) {
//        this.payService = payService;
        this.multiPaymentService = multiPaymentService;
    }

    @GetMapping("/customers/{customerId}/pending")
    @ResponseStatus(HttpStatus.OK)
    public PendingSalesResponse getPendingSales(@PathVariable Long customerId) {
        return multiPaymentService.getPendingSales(customerId);
    }

    @GetMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    public List<MultiPaymentResponse> getPaymentsByCustomer(@PathVariable Long customerId) {
        return multiPaymentService.getPaymentsByCustomer(customerId);
    }

    @GetMapping("/{id}/detail")
    @ResponseStatus(HttpStatus.OK)
    public MultiPaymentResponse getPaymentDetail(@PathVariable Long id) {
        return multiPaymentService.getPaymentDetail(id);
    }

    @PostMapping("/multi")
    @ResponseStatus(HttpStatus.CREATED)
    public MultiPaymentResponse createMultiPayment(@Valid @RequestBody MultiPaymentRequest request) {
        return multiPaymentService.processMultiPayment(request);
    }

    /*@GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Pay getPayment(@PathVariable Long id){
        return payService.findByIdOrThrow(id);
    }*/
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
    	multiPaymentService.deletePayment(id);
    }
	
}
