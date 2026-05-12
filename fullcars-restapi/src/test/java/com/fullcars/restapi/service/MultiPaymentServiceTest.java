package com.fullcars.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fullcars.restapi.dto.MultiPaymentRequest;
import com.fullcars.restapi.dto.MultiPaymentRequest.PaymentSplitRequest;
import com.fullcars.restapi.dto.MultiPaymentResponse;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.Pay;
import com.fullcars.restapi.model.PayAllocation;
import com.fullcars.restapi.model.PaymentSplit;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ICustomerCreditRepository;
import com.fullcars.restapi.repository.IPayAllocationRepository;
import com.fullcars.restapi.repository.IPayRepository;
import com.fullcars.restapi.repository.IPaymentSplitRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultiPaymentServiceTest {

    @Mock private CustomerService customerService;
    @Mock private SaleService saleService;
    @Mock private IPayRepository payRepository;
    @Mock private IPaymentSplitRepository paymentSplitRepository;
    @Mock private IPayAllocationRepository payAllocationRepository;
    @Mock private ICustomerCreditRepository customerCreditRepository;

    private MultiPaymentService service;

    private Customer customer;
    private Sale sale1;
    private Sale sale2;
    private long payIdCounter = 1;
    private long splitIdCounter = 1;

    @BeforeEach
    void setUp() {
        service = new MultiPaymentService(
                customerService, saleService, payRepository,
                paymentSplitRepository, payAllocationRepository, customerCreditRepository);

        customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Test Customer");
        customer.setCreditBalance(BigDecimal.ZERO);

        sale1 = createSale(10L, new BigDecimal("500.00"));
        sale2 = createSale(20L, new BigDecimal("300.00"));

        payIdCounter = 1;
        splitIdCounter = 1;
    }

    private Sale createSale(Long id, BigDecimal amount) {
        Sale sale = new Sale();
        sale.setId(id);
        sale.setCustomer(customer);
        sale.setDate(LocalDate.of(2026, 1, 15));
        sale.setSaleNumber("V-001");
        sale.setFactura(null);
        sale.setDetails(new ArrayList<>());
        com.fullcars.restapi.model.SaleDetail detail = new com.fullcars.restapi.model.SaleDetail();
        detail.setQuantity(1);
        detail.setUnitPrice(amount);
        detail.setSale(sale);
        sale.getDetails().add(detail);
        return sale;
    }

    private void setupCommonMocks() {
        when(customerService.findByIdOrThrow(1L)).thenReturn(customer);
        when(saleService.findAllByIds(List.of(10L, 20L))).thenReturn(List.of(sale1, sale2));
        when(payAllocationRepository.findBySaleId(anyLong())).thenReturn(List.of());
        when(payRepository.save(any(Pay.class))).thenAnswer(i -> {
            Pay p = (Pay) i.getArguments()[0];
            p.setId(payIdCounter++);
            return p;
        });
        when(paymentSplitRepository.save(any(PaymentSplit.class))).thenAnswer(i -> {
            PaymentSplit s = (PaymentSplit) i.getArguments()[0];
            s.setId(splitIdCounter++);
            return s;
        });
        when(payAllocationRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(customerService.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(customerCreditRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    private void assertZero(BigDecimal value) {
        assertEquals(0, value.compareTo(BigDecimal.ZERO), "Expected zero but got " + value);
    }

    @Test
    @DisplayName("Pago parcial: monto menor que la deuda, sin crédito")
    void testPartialPaymentWithoutCredit() {
        setupCommonMocks();

        PaymentSplitRequest split = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("600.00"))
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split))
                .useCredit(false)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("600.00"), response.getTotalAmount());
        assertZero(response.getCreditGenerated());
        assertZero(response.getCreditUsed());
        assertTrue(response.getSummary().contains("parcial"));
        assertFalse(response.getSummary().contains("Saldo a favor"));

        verify(customerCreditRepository, never()).save(any());

        ArgumentCaptor<PayAllocation> allocationCaptor = ArgumentCaptor.forClass(PayAllocation.class);
        verify(payAllocationRepository, atLeastOnce()).save(allocationCaptor.capture());

        System.out.println("=== Pago parcial: monto menor que la deuda, sin crédito ===");
        for (PayAllocation a : allocationCaptor.getAllValues()) {
            System.out.printf("  Venta #%d -> $%s (credito: %s)%n", a.getSale().getId(), a.getAmountApplied(), a.getIsCredit());
        }
        System.out.printf("  Total pagado: $%s, Pendiente: $200.00%n", response.getTotalAmount());

        BigDecimal totalSales = sale1.getTotal().add(sale2.getTotal());
        BigDecimal expectedPending = totalSales.subtract(response.getTotalAmount());
        assertEquals(0, expectedPending.compareTo(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("Pago exacto: monto igual a la deuda")
    void testExactPayment() {
        setupCommonMocks();

        PaymentSplitRequest split = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("800.00"))
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split))
                .useCredit(false)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("800.00"), response.getTotalAmount());
        assertZero(response.getCreditGenerated());
        assertZero(response.getCreditUsed());
        assertTrue(response.getSummary().contains("correctamente"));
        assertFalse(response.getSummary().contains("parcial"));
        assertFalse(response.getSummary().contains("Saldo a favor"));

        verify(customerCreditRepository, never()).save(any());
        
        BigDecimal totalSales = sale1.getTotal().add(sale2.getTotal());
        BigDecimal expectedPending = totalSales.subtract(response.getTotalAmount());
        assertEquals(0, expectedPending.compareTo(new BigDecimal("0")));
    }

    @Test
    @DisplayName("Sobrepago: genera crédito por excedente")
    void testOverpaymentGeneratesCredit() {
        setupCommonMocks();

        PaymentSplitRequest split = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("1000.00"))
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split))
                .useCredit(false)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.getTotalAmount());
        assertEquals(0, response.getCreditGenerated().compareTo(new BigDecimal("200.00")));
        assertZero(response.getCreditUsed());
        assertTrue(response.getSummary().contains("Saldo a favor"));
        assertEquals(0, customer.getCreditBalance().compareTo(new BigDecimal("200.00")));

        verify(customerCreditRepository, times(1)).save(argThat(credit ->
                credit.getAmount().compareTo(new BigDecimal("200.00")) == 0
        ));
        
        BigDecimal totalSales = sale1.getTotal().add(sale2.getTotal());
        BigDecimal expectedPending = totalSales.subtract(response.getTotalAmount());
        assertEquals(0, expectedPending.compareTo(new BigDecimal("-200.00")));
    }

    @Test
    @DisplayName("Pago con crédito: usa crédito disponible para cubrir deuda")
    void testPaymentWithCredit() {
        customer.setCreditBalance(new BigDecimal("200.00"));
        setupCommonMocks();

        PaymentSplitRequest split = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("600.00"))
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split))
                .useCredit(true)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("800.00"), response.getTotalAmount());
        assertEquals(0, response.getCreditUsed().compareTo(new BigDecimal("200.00")));
        assertZero(response.getCreditGenerated());
        assertZero(customer.getCreditBalance());
        
        System.out.println("====== Pago con credito: usa credito para curbrir deuda con monto exacto=======\n"+response.getSplits()+'\n');
        
        BigDecimal totalSales = sale1.getTotal().add(sale2.getTotal());
        BigDecimal expectedPending = totalSales.subtract(response.getTotalAmount());
        assertEquals(0, expectedPending.compareTo(new BigDecimal("0")));
    }

    @Test
    @DisplayName("Pago parcial con crédito insuficiente: paga parcial, no falla")
    void testPartialPaymentWithInsufficientCredit() {
        customer.setCreditBalance(new BigDecimal("50.00"));
        setupCommonMocks();

        PaymentSplitRequest split = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("600.00"))
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split))
                .useCredit(true)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("650.00"), response.getTotalAmount());
        assertEquals(0, response.getCreditUsed().compareTo(new BigDecimal("50.00")));
        assertZero(response.getCreditGenerated());
        assertTrue(response.getSummary().contains("parcial"));
        assertZero(customer.getCreditBalance());

        ArgumentCaptor<PayAllocation> allocationCaptor2 = ArgumentCaptor.forClass(PayAllocation.class);
        verify(payAllocationRepository, atLeastOnce()).save(allocationCaptor2.capture());

        System.out.println("=== Pago parcial con crédito insuficiente ===");
        for (PayAllocation a : allocationCaptor2.getAllValues()) {
            System.out.printf("  Venta #%d -> $%s (credito: %s)%n", a.getSale().getId(), a.getAmountApplied(), a.getIsCredit());
        }
        System.out.printf("  Total pagado: $%s (efectivo: $600 + credito: $50), Pendiente: $150.00%n", response.getTotalAmount());
        
        BigDecimal totalSales = sale1.getTotal().add(sale2.getTotal());
        BigDecimal expectedPending = totalSales.subtract(response.getTotalAmount());
        assertEquals(0, expectedPending.compareTo(new BigDecimal("150.00")));
    }

    @Test
    @DisplayName("Pago parcial con múltiples splits")
    void testPartialPaymentWithMultipleSplits() {
        setupCommonMocks();

        PaymentSplitRequest split1 = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("300.00"))
                .build();

        PaymentSplitRequest split2 = PaymentSplitRequest.builder()
                .paymentMethod("Cheque")
                .amount(new BigDecimal("200.00"))
                .reference("12345")
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split1, split2))
                .useCredit(false)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getTotalAmount());
        assertZero(response.getCreditGenerated());
        assertZero(response.getCreditUsed());
        assertTrue(response.getSummary().contains("parcial"));
        assertEquals(2, response.getSplits().size());
        
        BigDecimal totalSales = sale1.getTotal().add(sale2.getTotal());
        BigDecimal expectedPending = totalSales.subtract(response.getTotalAmount());
        assertEquals(0, expectedPending.compareTo(new BigDecimal("300.00")));
    }

    @Test
    @DisplayName("Pago completo con múltiples splits")
    void testFullPaymentWithMultipleSplits() {
        setupCommonMocks();

        PaymentSplitRequest split1 = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("500.00"))
                .build();

        PaymentSplitRequest split2 = PaymentSplitRequest.builder()
                .paymentMethod("Tarjeta")
                .amount(new BigDecimal("300.00"))
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split1, split2))
                .useCredit(false)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("800.00"), response.getTotalAmount());
        assertZero(response.getCreditGenerated());
        assertZero(response.getCreditUsed());
        assertTrue(response.getSummary().contains("correctamente"));
        assertEquals(2, response.getSplits().size());
    }

    @Test
    @DisplayName("Pago con crédito que cubre más de la deuda: solo usa lo necesario")
    void testPaymentWithCreditMoreThanNeeded() {
        customer.setCreditBalance(new BigDecimal("500.00"));
        setupCommonMocks();

        PaymentSplitRequest split = PaymentSplitRequest.builder()
                .paymentMethod("Efectivo")
                .amount(new BigDecimal("600.00"))
                .build();

        MultiPaymentRequest request = MultiPaymentRequest.builder()
                .customerId(1L)
                .saleIds(List.of(10L, 20L))
                .splits(List.of(split))
                .useCredit(true)
                .date(LocalDate.now())
                .build();

        MultiPaymentResponse response = service.processMultiPayment(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("800.00"), response.getTotalAmount());
        assertEquals(0, response.getCreditUsed().compareTo(new BigDecimal("200.00")));
        assertZero(response.getCreditGenerated());
        assertEquals(0, customer.getCreditBalance().compareTo(new BigDecimal("300.00")));
    }
}