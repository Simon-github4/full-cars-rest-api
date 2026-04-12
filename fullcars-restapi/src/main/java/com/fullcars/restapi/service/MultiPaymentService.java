package com.fullcars.restapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.dto.MultiPaymentRequest;
import com.fullcars.restapi.dto.MultiPaymentResponse;
import com.fullcars.restapi.dto.MultiPaymentResponse.AllocationInfo;
import com.fullcars.restapi.dto.MultiPaymentResponse.CreditInfo;
import com.fullcars.restapi.dto.MultiPaymentResponse.SaleUpdate;
import com.fullcars.restapi.dto.PendingSalesResponse;
import com.fullcars.restapi.dto.PendingSalesResponse.SalePendingInfo;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.CustomerCredit;
import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.model.PayAllocation;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ICustomerCreditRepository;
import com.fullcars.restapi.repository.ICustomerRepository;
import com.fullcars.restapi.repository.IPayAllocationRepository;
import com.fullcars.restapi.repository.IPayRepository;
import com.fullcars.restapi.repository.ISaleRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MultiPaymentService {

    private final ICustomerRepository customerRepository;
    private final ISaleRepository saleRepository;
    private final IPayRepository payRepository;
    private final IPayAllocationRepository payAllocationRepository;
    private final ICustomerCreditRepository customerCreditRepository;

    public MultiPaymentService(
            ICustomerRepository customerRepository,
            ISaleRepository saleRepository,
            IPayRepository payRepository,
            IPayAllocationRepository payAllocationRepository,
            ICustomerCreditRepository customerCreditRepository) {
        this.customerRepository = customerRepository;
        this.saleRepository = saleRepository;
        this.payRepository = payRepository;
        this.payAllocationRepository = payAllocationRepository;
        this.customerCreditRepository = customerCreditRepository;
    }

    public PendingSalesResponse getPendingSales(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + customerId));

        List<Sale> sales = saleRepository.findByCustomerIdOrderByDate(customerId);
        List<SalePendingInfo> pendingSales = new ArrayList<>();
        BigDecimal totalPending = BigDecimal.ZERO;

        for (Sale sale : sales) {
            BigDecimal total = sale.getTotal();
            BigDecimal totalPaid = getTotalPaidForSale(sale.getId());
            BigDecimal remaining = total.subtract(totalPaid).setScale(2, RoundingMode.HALF_UP);

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                pendingSales.add(SalePendingInfo.builder()
                        .saleId(sale.getId())
                        .date(sale.getDate())
                        .total(total)
                        .totalPaid(totalPaid)
                        .remainingDue(remaining)
                        .facturaId(sale.getFactura() != null ? sale.getFactura().getId() : null)
                        .invoiceNumber(sale.getFactura() != null ? 
                                sale.getFactura().getNumeroComprobante().toString() : null)
                        .build());
                totalPending = totalPending.add(remaining);
            }
        }

        return PendingSalesResponse.builder()
                .customerId(customerId)
                .customerName(customer.getFullName())
                .totalPending(totalPending)
                .creditBalance(customer.getCreditBalance())
                .sales(pendingSales)
                .build();
    }

    @Transactional
    public MultiPaymentResponse processMultiPayment(MultiPaymentRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + request.getCustomerId()));

        List<Sale> salesToPay;
        if (request.getSaleIds() != null && !request.getSaleIds().isEmpty()) {
            salesToPay = saleRepository.findAllById(request.getSaleIds());
            for (Sale s : salesToPay) 
                if (!s.getCustomer().getId().equals(request.getCustomerId())) 
                    throw new IllegalArgumentException("La venta " + s.getId() + " no pertenece al cliente");
            
        } else 
            salesToPay = saleRepository.findByCustomerIdOrderByDate(request.getCustomerId());
        

        List<Sale> pendingSales = salesToPay.stream()
                .filter(s -> getRemainingDue(s).compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (pendingSales.isEmpty()) {
            throw new IllegalArgumentException("No hay ventas pendientes para pagar");
        }

        BigDecimal totalDebt = pendingSales.stream()
                .map(this::getRemainingDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditUsed = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getUseCredit())) {
            BigDecimal faltaCubrir = totalDebt.subtract(request.getPaymentAmount());
            if (faltaCubrir.compareTo(BigDecimal.ZERO) > 0) {
                if (customer.getCreditBalance().compareTo(faltaCubrir) < 0) {
                    throw new IllegalArgumentException(String.format(
                        "Credito insuficiente. Necesitas $%.2f, tienes $%.2f de credito",
                        faltaCubrir, customer.getCreditBalance()));
                }
                creditUsed = faltaCubrir;
            }
        }

        BigDecimal effectivePayment = request.getPaymentAmount().add(creditUsed);
        
        if (effectivePayment.compareTo(totalDebt) < 0) {
            throw new IllegalArgumentException(String.format(
                "Monto insuficiente. Total pendiente: $%.2f, Monto disponible (pago + credito): $%.2f, Faltan: $%.2f",
                totalDebt, effectivePayment, totalDebt.subtract(effectivePayment)));
        }
        
        BigDecimal creditGenerated = BigDecimal.ZERO;

        Pay payment = Pay.builder()
                .amount(request.getPaymentAmount())
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .paymentMethod(request.getPaymentMethod())
                .customer(customer)
                .description(request.getNotes())
                .creditUsed(creditUsed)
                .creditGenerated(BigDecimal.ZERO)
                .build();
        payment = payRepository.save(payment);

        List<AllocationInfo> allocations = new ArrayList<>();
        List<SaleUpdate> salesUpdated = new ArrayList<>();

        BigDecimal remainingCash = request.getPaymentAmount();
        BigDecimal remainingCredit = creditUsed;

        for (Sale sale : pendingSales) {
            if (remainingCash.compareTo(BigDecimal.ZERO) <= 0 && remainingCredit.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal saleRemaining = getRemainingDue(sale.getId());
            BigDecimal cashApplied = BigDecimal.ZERO;
            BigDecimal creditApplied = BigDecimal.ZERO;

            if (remainingCash.compareTo(BigDecimal.ZERO) > 0) {
                cashApplied = remainingCash.min(saleRemaining);
                remainingCash = remainingCash.subtract(cashApplied);
                saleRemaining = saleRemaining.subtract(cashApplied);

                PayAllocation cashAllocation = PayAllocation.builder()
                        .pay(payment)
                        .sale(sale)
                        .amountApplied(cashApplied)
                        .isCredit(false)
                        .build();
                payAllocationRepository.save(cashAllocation);

                allocations.add(AllocationInfo.builder()
                        .saleId(sale.getId())
                        .saleTotal(sale.getTotal())
                        .amountApplied(cashApplied)
                        .build());
            }

            if (remainingCredit.compareTo(BigDecimal.ZERO) > 0 && saleRemaining.compareTo(BigDecimal.ZERO) > 0) {
                creditApplied = remainingCredit.min(saleRemaining);
                remainingCredit = remainingCredit.subtract(creditApplied);

                PayAllocation creditAllocation = PayAllocation.builder()
                        .pay(payment)
                        .sale(sale)
                        .amountApplied(creditApplied)
                        .isCredit(true)
                        .build();
                payAllocationRepository.save(creditAllocation);

                allocations.add(AllocationInfo.builder()
                        .saleId(sale.getId())
                        .saleTotal(sale.getTotal())
                        .amountApplied(creditApplied)
                        .build());
            }

            BigDecimal totalAppliedToSale = cashApplied.add(creditApplied);
            BigDecimal newTotalPaid = getTotalPaidForSale(sale.getId());
            BigDecimal newRemaining = sale.getTotal().subtract(newTotalPaid).setScale(2, RoundingMode.HALF_UP);
            boolean paid = newRemaining.compareTo(BigDecimal.ZERO) <= 0;

            salesUpdated.add(SaleUpdate.builder()
                    .saleId(sale.getId())
                    .total(sale.getTotal())
                    .totalPaid(newTotalPaid)
                    .remainingDue(newRemaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newRemaining)
                    .paid(paid)
                    .build());
        }

        BigDecimal totalRemaining = remainingCash.add(remainingCredit);
        if (totalRemaining.compareTo(BigDecimal.ZERO) > 0) {
            creditGenerated = totalRemaining;
            payment.setCreditGenerated(creditGenerated);
            payRepository.save(payment);

            CustomerCredit newCredit = CustomerCredit.builder()
                    .customer(customer)
                    .amount(creditGenerated)
                    .description("Saldo a favor por exceso de pago")
                    .payId(payment.getId())
                    .build();
            customerCreditRepository.save(newCredit);

            customer.setCreditBalance(customer.getCreditBalance().add(creditGenerated));
        }

        if (creditUsed.compareTo(BigDecimal.ZERO) > 0) {
            customer.setCreditBalance(customer.getCreditBalance().subtract(creditUsed));
        }
        customerRepository.save(customer);

        String summary;
        if (creditGenerated.compareTo(BigDecimal.ZERO) > 0) {
            summary = String.format("Pago realizado. Saldo a favor generado: $%.2f", creditGenerated);
        } else {
            summary = "Pago aplicado correctamente a las ventas seleccionadas";
        }

        return MultiPaymentResponse.builder()
                .paymentId(payment.getId())
                .customerId(customer.getId())
                .paymentAmount(request.getPaymentAmount())
                .creditUsed(creditUsed)
                .date(payment.getDate())
                .paymentMethod(payment.getPaymentMethod())
                .allocations(allocations)
                .salesUpdated(salesUpdated)
                .creditGenerated(creditGenerated.compareTo(BigDecimal.ZERO) > 0 ?
                        CreditInfo.builder().amount(creditGenerated)
                                .description("Saldo a favor generado").build() : null)
                .customerCreditBalance(customer.getCreditBalance())
                .summary(summary)
                .build();
    }

    @Transactional
    public void deletePayment(Long payId) {
        Pay pay = payRepository.findById(payId).orElseThrow();
        Customer customer = pay.getCustomer();
        
        BigDecimal creditUsed = pay.getCreditUsed();
        BigDecimal creditGenerated = pay.getCreditGenerated();
        
        // Eliminar allocations
        payAllocationRepository.deleteByPayId(payId);
        
        // Ajustar crédito del cliente
        if (creditUsed != null && creditUsed.compareTo(BigDecimal.ZERO) > 0) {
            customer.setCreditBalance(customer.getCreditBalance().add(creditUsed)); // + vuelve
        }
        
        if (creditGenerated != null && creditGenerated.compareTo(BigDecimal.ZERO) > 0) {
            customer.setCreditBalance(customer.getCreditBalance().subtract(creditGenerated)); // - se pierde
        }
        
        customerRepository.save(customer);
        payRepository.delete(pay);
    }
    
    private BigDecimal getTotalPaidForSale(Long saleId) {
        return payAllocationRepository.findBySaleId(saleId).stream()
                .map(PayAllocation::getAmountApplied)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getRemainingDue(Sale sale) {
        BigDecimal total = sale.getTotal();
        BigDecimal paid = getTotalPaidForSale(sale.getId());
        return total.subtract(paid).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getRemainingDue(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new EntityNotFoundException("Venta no encontrada: " + saleId));
        return getRemainingDue(sale);
    }

    public List<MultiPaymentResponse> getPaymentsByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + customerId));

        List<Pay> payments = payRepository.findByCustomerId(customerId);
        List<MultiPaymentResponse> responses = new ArrayList<>();

        for (Pay pay : payments) {
            MultiPaymentResponse response = buildPaymentResponse(pay);
            responses.add(response);
        }

        return responses;
    }

    public MultiPaymentResponse getPaymentDetail(Long payId) {
        Pay pay = payRepository.findById(payId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + payId));
        return buildPaymentResponse(pay);
    }

    private MultiPaymentResponse buildPaymentResponse(Pay pay) {
        List<PayAllocation> allocations = payAllocationRepository.findByPayId(pay.getId());
        
        List<AllocationInfo> allocationInfos = allocations.stream()
                .map(a -> AllocationInfo.builder()
                        .saleId(a.getSale().getId())
                        .saleTotal(a.getSale().getTotal())
                        .amountApplied(a.getAmountApplied())
                        .isCredit(a.getIsCredit())
                        .build())
                .toList();

        List<SaleUpdate> salesUpdated = allocations.stream()
                .map(a -> {
                    BigDecimal totalPaid = getTotalPaidForSale(a.getSale().getId());
                    BigDecimal remaining = a.getSale().getTotal().subtract(totalPaid).setScale(2, RoundingMode.HALF_UP);
                    return SaleUpdate.builder()
                            .saleId(a.getSale().getId())
                            .total(a.getSale().getTotal())
                            .totalPaid(totalPaid)
                            .remainingDue(remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining)
                            .paid(remaining.compareTo(BigDecimal.ZERO) <= 0)
                            .build();
                })
                .toList();

        return MultiPaymentResponse.builder()
                .paymentId(pay.getId())
                .customerId(pay.getCustomer().getId())
                .paymentAmount(pay.getAmount())
                .creditUsed(pay.getCreditUsed())
                .date(pay.getDate())
                .paymentMethod(pay.getPaymentMethod())
                .allocations(allocationInfos)
                .salesUpdated(salesUpdated)
                .creditGenerated(pay.getCreditGenerated() != null && pay.getCreditGenerated().compareTo(BigDecimal.ZERO) > 0 ?
                        CreditInfo.builder().amount(pay.getCreditGenerated()).description("Saldo a favor generado").build() : null)
                .customerCreditBalance(pay.getCustomer().getCreditBalance())
                .summary("Pago #" + pay.getId())
                .build();
    }
}
