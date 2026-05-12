package com.fullcars.restapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.dto.MultiPaymentRequest;
import com.fullcars.restapi.dto.MultiPaymentRequest.PaymentSplitRequest;
import com.fullcars.restapi.dto.MultiPaymentResponse;
import com.fullcars.restapi.dto.PaymentSplitDTO;
import com.fullcars.restapi.dto.PendingSalesResponse;
import com.fullcars.restapi.dto.PendingSalesResponse.SalePendingInfo;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.CustomerCredit;
import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.model.PayAllocation;
import com.fullcars.restapi.model.PaymentSplit;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ICustomerCreditRepository;
import com.fullcars.restapi.repository.IPayAllocationRepository;
import com.fullcars.restapi.repository.IPayRepository;
import com.fullcars.restapi.repository.IPaymentSplitRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MultiPaymentService {

    private final CustomerService customerService;
    private final SaleService saleService;
    private final IPayRepository payRepository;
    private final IPaymentSplitRepository paymentSplitRepository;
    private final IPayAllocationRepository payAllocationRepository;
    private final ICustomerCreditRepository customerCreditRepository;

    public MultiPaymentService(
            CustomerService customerService,
            SaleService saleService,
            IPayRepository payRepository,
            IPaymentSplitRepository paymentSplitRepository,
            IPayAllocationRepository payAllocationRepository,
            ICustomerCreditRepository customerCreditRepository) {
        this.customerService = customerService;
        this.saleService = saleService;
        this.payRepository = payRepository;
        this.paymentSplitRepository = paymentSplitRepository;
        this.payAllocationRepository = payAllocationRepository;
        this.customerCreditRepository = customerCreditRepository;
    }

    public PendingSalesResponse getPendingSales(Long customerId) {
        Customer customer = customerService.findByIdOrThrow(customerId);

        List<Sale> sales = saleService.findByCustomerIdOrderByDate(customerId);
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
                        .cae(sale.getFactura() != null ? sale.getFactura().getCae() : null)
                        .saleNumber(sale.getSaleNumber())
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
        Customer customer = customerService.findByIdOrThrow(request.getCustomerId());

        List<Sale> salesToPay;
        if (request.getSaleIds() != null && !request.getSaleIds().isEmpty()) {
            salesToPay = saleService.findAllByIds(request.getSaleIds());
            for (Sale s : salesToPay) {
                if (!s.getCustomer().getId().equals(request.getCustomerId())) {
                    throw new IllegalArgumentException("La venta " + s.getId() + " no pertenece al cliente");
                }
            }
        } else {
            salesToPay = saleService.findByCustomerIdOrderByDate(request.getCustomerId());
        }

        List<Sale> pendingSales = salesToPay.stream()
                .filter(s -> getRemainingDue(s).compareTo(BigDecimal.ZERO) > 0)
                .toList();

        if (pendingSales.isEmpty()) {
            throw new IllegalArgumentException("No hay ventas pendientes para pagar");
        }

        BigDecimal totalDebt = pendingSales.stream()
                .map(this::getRemainingDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSplits = request.getSplits().stream()
                .map(PaymentSplitRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditUsed = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.getUseCredit())) {
            BigDecimal faltaCubrir = totalDebt.subtract(totalSplits);
            if (faltaCubrir.compareTo(BigDecimal.ZERO) > 0) {
                if (customer.getCreditBalance().compareTo(faltaCubrir) < 0) {
                    creditUsed = customer.getCreditBalance();
                } else {
                    creditUsed = faltaCubrir;
                }
            }
        }

        BigDecimal effectivePayment = totalSplits.add(creditUsed);

        if (effectivePayment.compareTo(totalDebt) < 0) {
        }

        BigDecimal creditGenerated = BigDecimal.ZERO;

        Pay payment = Pay.builder()
                .customer(customer)
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .description(request.getNotes())
                .creditUsed(creditUsed)
                .creditGenerated(BigDecimal.ZERO)
                .build();
        payment = payRepository.save(payment);

        Map<Long, BigDecimal> remainingPerSale = pendingSales.stream()
                .collect(Collectors.toMap(Sale::getId, this::getRemainingDue));

        List<PaymentSplitDTO> splitDTOs = new ArrayList<>();

        for (PaymentSplitRequest splitReq : request.getSplits()) {
            PaymentSplit split = PaymentSplit.builder()
                    .pay(payment)
                    .amount(splitReq.getAmount())
                    .paymentMethod(splitReq.getPaymentMethod())
                    .reference(splitReq.getReference())
                    .build();
            split = paymentSplitRepository.save(split);

            List<String> salesCovered = new ArrayList<>();
            BigDecimal remainingInSplit = splitReq.getAmount();

            for (Sale sale : pendingSales) {
                BigDecimal saleRemaining = remainingPerSale.getOrDefault(sale.getId(), BigDecimal.ZERO);
                if (saleRemaining.compareTo(BigDecimal.ZERO) <= 0 || remainingInSplit.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal amountToApply = remainingInSplit.min(saleRemaining);

                PayAllocation allocation = PayAllocation.builder()
                        .paymentSplit(split)
                        .sale(sale)
                        .amountApplied(amountToApply)
                        .isCredit(false)
                        .build();
                payAllocationRepository.save(allocation);

                remainingPerSale.put(sale.getId(), saleRemaining.subtract(amountToApply));
                remainingInSplit = remainingInSplit.subtract(amountToApply);

                salesCovered.add(sale.getId().toString());

                if (remainingInSplit.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
            }

            splitDTOs.add(PaymentSplitDTO.builder()
                    .splitId(split.getId())
                    .amount(splitReq.getAmount())
                    .paymentMethod(splitReq.getPaymentMethod())
                    .reference(splitReq.getReference())
                    .salesCovered(salesCovered)
                    .build());
        }

        if (creditUsed.compareTo(BigDecimal.ZERO) > 0) {
            PaymentSplit creditSplit = PaymentSplit.builder()
                    .pay(payment)
                    .amount(creditUsed)
                    .paymentMethod("Credito a favor")
                    .build();
            creditSplit = paymentSplitRepository.save(creditSplit);

            List<String> salesCovered = new ArrayList<>();

            for (Sale sale : pendingSales) {
                BigDecimal saleRemaining = remainingPerSale.getOrDefault(sale.getId(), BigDecimal.ZERO);
                if (saleRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal amountToApply = creditUsed.min(saleRemaining);

                PayAllocation allocation = PayAllocation.builder()
                        .paymentSplit(creditSplit)
                        .sale(sale)
                        .amountApplied(amountToApply)
                        .isCredit(true)
                        .build();
                payAllocationRepository.save(allocation);

                remainingPerSale.put(sale.getId(), saleRemaining.subtract(amountToApply));

                salesCovered.add(sale.getId().toString());
            }

            splitDTOs.add(PaymentSplitDTO.builder()
                    .splitId(creditSplit.getId())
                    .amount(creditUsed)
                    .paymentMethod("credito")
                    .salesCovered(salesCovered)
                    .build());
        }

        if (effectivePayment.compareTo(totalDebt) > 0) {
            creditGenerated = effectivePayment.subtract(totalDebt);
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
        customerService.save(customer);

        BigDecimal totalAmount = splitDTOs.stream()
                .map(PaymentSplitDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String summary;
        if (creditGenerated.compareTo(BigDecimal.ZERO) > 0) {
            summary = String.format("Pago realizado. Saldo a favor generado: $%.2f", creditGenerated);
        } else if (effectivePayment.compareTo(totalDebt) < 0) {
            summary = String.format("Pago parcial aplicado. Restan $%.2f por cubrir", totalDebt.subtract(effectivePayment));
        } else {
            summary = "Pago aplicado correctamente a las ventas seleccionadas";
        }

        return MultiPaymentResponse.builder()
                .paymentId(payment.getId())
                .customerId(customer.getId())
                .date(payment.getDate())
                .description(payment.getDescription())
                .totalAmount(totalAmount)
                .creditUsed(creditUsed)
                .creditGenerated(creditGenerated)
                .customerCreditBalance(customer.getCreditBalance())
                .splits(splitDTOs)
                .summary(summary)
                .build();
    }

    @Transactional
    public void deletePayment(Long payId) {
        Pay pay = payRepository.findById(payId).orElseThrow();
        Customer customer = pay.getCustomer();

        BigDecimal creditUsed = pay.getCreditUsed();
        BigDecimal creditGenerated = pay.getCreditGenerated();

        List<PaymentSplit> splits = pay.getSplits();
        for (PaymentSplit split : splits) {
        	for (PayAllocation alloc : split.getAllocations()) {
        		payAllocationRepository.delete(alloc);
        	}
            paymentSplitRepository.delete(split);
        }

        if (creditUsed != null && creditUsed.compareTo(BigDecimal.ZERO) > 0) {
            customer.setCreditBalance(customer.getCreditBalance().add(creditUsed));
        }

        if (creditGenerated != null && creditGenerated.compareTo(BigDecimal.ZERO) > 0) {
            customer.setCreditBalance(customer.getCreditBalance().subtract(creditGenerated));
        }

        customerService.save(customer);
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

    @Transactional(readOnly = true)
    public List<MultiPaymentResponse> getPaymentsByCustomer(Long customerId) {
        customerService.findByIdOrThrow(customerId);

        List<Pay> payments = payRepository.findByCustomerId(customerId);
        List<MultiPaymentResponse> responses = new ArrayList<>();

        for (Pay pay : payments) {
            responses.add(buildPaymentResponse(pay));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public MultiPaymentResponse getPaymentDetail(Long payId) {
        Pay pay = payRepository.findById(payId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + payId));
        return buildPaymentResponse(pay);
    }
    
@Transactional(readOnly = true)
    private MultiPaymentResponse buildPaymentResponse(Pay pay) {
        List<PaymentSplit> splits = paymentSplitRepository.findByPayId(pay.getId());
        
        List<PaymentSplitDTO> splitDTOs = new ArrayList<>();
        
        for (PaymentSplit split : splits) {
            List<PayAllocation> allocations = payAllocationRepository.findBySplitId(split.getId());
            
            List<String> salesCovered = new ArrayList<>();
            for (PayAllocation a : allocations) 
                if (a.getSale() != null) 
                    salesCovered.add(a.getSale().getId().toString());
            
            splitDTOs.add(PaymentSplitDTO.builder()
                    .splitId(split.getId())
                    .amount(split.getAmount())
                    .paymentMethod(split.getPaymentMethod())
                    .reference(split.getReference())
                    .salesCovered(salesCovered)
                    .build());
        }

        return MultiPaymentResponse.builder()
                .paymentId(pay.getId())
                .customerId(pay.getCustomer().getId())
                .date(pay.getDate())
                .description(pay.getDescription())
                .totalAmount(pay.getTotalAmount())
                .creditUsed(pay.getCreditUsed())
                .creditGenerated(pay.getCreditGenerated())
                .customerCreditBalance(pay.getCustomer().getCreditBalance())
                .splits(splitDTOs)
                .summary("Pago #" + pay.getId())
                .build();
    }
}