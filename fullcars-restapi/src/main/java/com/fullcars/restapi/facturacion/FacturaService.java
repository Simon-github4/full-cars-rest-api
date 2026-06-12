package com.fullcars.restapi.facturacion;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.ServerException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fullcars.restapi.dto.AfipAuth;
import com.fullcars.restapi.dto.CAEResponse;
import com.fullcars.restapi.dto.ContribuyenteData;
import com.fullcars.restapi.dto.DatosFacturacion;
import com.fullcars.restapi.facturacion.enums.Conceptos;
import com.fullcars.restapi.facturacion.enums.CondicionIva;
import com.fullcars.restapi.facturacion.enums.Servicios;
import com.fullcars.restapi.facturacion.enums.TiposComprobante;
import com.fullcars.restapi.model.Comprobante;
import com.fullcars.restapi.model.CreditNote;
import com.fullcars.restapi.model.Customer;
import com.fullcars.restapi.model.CustomerCredit;
import com.fullcars.restapi.model.Factura;
import com.fullcars.restapi.model.Sale;
import com.fullcars.restapi.repository.ICreditNoteRepository;
import com.fullcars.restapi.repository.ICustomerCreditRepository;
import com.fullcars.restapi.repository.IFacturaRepository;
import com.fullcars.restapi.service.CustomerService;
import com.fullcars.restapi.service.SaleService;

@Service
public class FacturaService {

    private static final String RUTA_BASE_PDFS = "C:/SoftwareFullCars/FacturasEmitidas/";

    private final IFacturaRepository facturaRepository;
    private final ICreditNoteRepository creditNoteRepository;
    private final ICustomerCreditRepository customerCreditRepository;
    private final SaleService saleService;
    private final CustomerService customerService;
    private final ArcaTokenCacheService tokenService;
    private final AfipConfig afipConfig;

    public FacturaService(
            IFacturaRepository facturaRepository,
            ICreditNoteRepository creditNoteRepository,
            ICustomerCreditRepository customerCreditRepository,
            SaleService saleService,
            CustomerService customerService,
            ArcaTokenCacheService tokenService,
            AfipConfig afipConfig) {
        this.facturaRepository = facturaRepository;
        this.creditNoteRepository = creditNoteRepository;
        this.customerCreditRepository = customerCreditRepository;
        this.saleService = saleService;
        this.customerService = customerService;
        this.tokenService = tokenService;
        this.afipConfig = afipConfig;
    }

    @Transactional
    public byte[] generarFactura(Long saleId, TiposComprobante tiposComprobante) throws Exception {
        if (!tiposComprobante.isFactura()) {
            throw new IllegalArgumentException("El tipo de comprobante solicitado no es una factura.");
        }

        Sale sale = saleService.findByIdOrThrow(saleId);
        if (sale.getFactura() != null) {
            throw new RuntimeException("La venta ya posee una factura electronica asociada.");
        }

        DatosFacturacion datosFact = new DatosFacturacion(afipConfig.getCuit(), tiposComprobante,
                Long.parseLong(sale.getCustomer().getCuit()));
        ContribuyenteData receptor = resolverReceptor(datosFact);
        CAEResponse response = emitirCaeFactura(sale, datosFact);
        Factura factura = mapearComprobante(new Factura(), sale, datosFact, receptor, response, null);

        if (response != null && response.getCae() != null) {
            try {
                Factura saved = facturaRepository.save(factura);
                sale.setFactura(saved);
                saleService.update(sale);

                byte[] pdfBytes = FacturaPDFGenerator.generarFacturaPDF(saved, sale, datosFact.getAlicuota());
                saved.setFileUrl(guardarPdfEnDisco(pdfBytes, generarNombreArchivo(saved)));
                facturaRepository.save(saved);
                return pdfBytes;
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("CAE generado. Error al generar o guardar el PDF");
            }
        }

        throw new RuntimeException("No se pudo generar la factura electronica: "
                + (response != null ? response.getError() : "sin respuesta de ARCA"));
    }

    @Transactional
    public byte[] generarNotaCreditoBySale(Long saleId, BigDecimal monto) throws Exception {
        Factura facturaAsociada = obtenerFacturaOriginal(saleId);
        Sale sale = saleService.findByFacturaIdOrThrow(facturaAsociada.getId());
        return generarNotaCredito(facturaAsociada, sale, monto);
    }

    private byte[] generarNotaCredito(Factura facturaAsociada, Sale sale, BigDecimal monto) throws Exception {
        if (creditNoteRepository.findFirstByComprobanteAsociadoIdOrderByIdDesc(facturaAsociada.getId()).isPresent()) {
            throw new RuntimeException("La factura ya posee una nota de credito electronica asociada.");
        }
        
        //if (monto == null) monto = facturaOriginal.getImpTotal();
        if (monto.compareTo(BigDecimal.ZERO) <= 0
                || monto.compareTo(facturaAsociada.getImpTotal()) > 0) {
            throw new RuntimeException(
                    "El monto de la nota de credito debe ser mayor a 0 y no exceder el total de la factura original.");
        }

        TiposComprobante tipoNotaCredito = TiposComprobante
                .notaCreditoPara(TiposComprobante.fromCodigo(facturaAsociada.getTipoComprobante()));
        DatosFacturacion datosFact = new DatosFacturacion(afipConfig.getCuit(), tipoNotaCredito,
        		facturaAsociada.getCuitCliente());
        ContribuyenteData receptor = receptorDesdeComprobante(facturaAsociada);

        CAEResponse response = emitirCaeNC(sale, datosFact, facturaAsociada, monto);
        CreditNote creditNote = mapearComprobante(new CreditNote(), sale, datosFact, receptor, response, monto);
        creditNote.setComprobanteAsociado(facturaAsociada);

        if (response != null && response.getCae() != null) {
            try {
                CreditNote saved = creditNoteRepository.save(creditNote);

                CustomerCredit customerCredit = crearCreditoCliente(sale.getCustomer(), saved);
                saved.setCustomerCredit(customerCredit);

                byte[] pdfBytes = FacturaPDFGenerator.generarFacturaPDF(saved, sale, datosFact.getAlicuota());
                saved.setFileUrl(guardarPdfEnDisco(pdfBytes, generarNombreArchivo(saved)));
                creditNoteRepository.save(saved);
                return pdfBytes;
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("CAE generado. Error al generar o guardar el PDF de nota de credito");
            }
        }

        throw new RuntimeException("No se pudo generar la nota de credito electronica: "
                + (response != null ? response.getError() : "sin respuesta de ARCA"));
    }

    public File getFacturaPdf(Long saleId) throws ServerException {
        Factura factura = obtenerFacturaOriginal(saleId);
        Path filePath = Paths.get(factura.getFileUrl());

        File file = filePath.toFile();
        if (!file.exists()) {
            throw new ServerException("El PDF de factura no existe en el servidor");
        }

        return file;
    }

    public Factura findBySaleId(Long idSale) {
        return obtenerFacturaOriginal(idSale);
    }

    public boolean isSaleFacturada(Long idSale) {
        return saleService.findFacturaBySaleId(idSale).isPresent();
    }

    public boolean isNotaCreditoEmitida(Long saleId) {
        return saleService.findFacturaBySaleId(saleId)
                .map(factura -> creditNoteRepository.findFirstByComprobanteAsociadoIdOrderByIdDesc(factura.getId()).isPresent())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public CreditNote findNotaCreditoBySaleId(Long saleId) {
        Factura factura = obtenerFacturaOriginal(saleId);
        return creditNoteRepository.findFirstByComprobanteAsociadoIdOrderByIdDesc(factura.getId())
                .orElseThrow(() -> new RuntimeException("No se encontró nota de crédito para la venta " + saleId));
    }

    public String getAfipData() {
        return tokenService.getTicket(Servicios.CONSTANCIA_INSCRIPCION).toString()
                .concat(afipConfig.getWsfev1Endpoint());
    }

    private CustomerCredit crearCreditoCliente(Customer customer, CreditNote creditNote) {
        CustomerCredit customerCredit = CustomerCredit.builder()
                .customer(customer)
                .amount(creditNote.getImpTotal())
                .description("Saldo a favor por nota de credito " + generarNumeroComprobante(creditNote))
                .build();
        customerCredit = customerCreditRepository.save(customerCredit);

        customer.setCreditBalance(customer.getCreditBalance().add(creditNote.getImpTotal()));
        customerService.save(customer);
        return customerCredit;
    }

    //===============================   METODOS AUX ARCA    ============================================================================

    private CAEResponse emitirCaeFactura(Sale sale, DatosFacturacion datosFact) throws Exception{
    	return emitirCaeNC(sale, datosFact, null, null);
    }

    private CAEResponse emitirCaeNC(Sale sale, DatosFacturacion datosFact, Factura comprobanteAsociado,
            BigDecimal totalOverride) throws Exception {
        AfipAuth auth = tokenService.getTicket(Servicios.FACTURACION_ELECTRONICA);
        long ultimoComp = WSFEV1Consultas.consultarUltimoComprobanteFEV1(
                auth,
                datosFact,
                afipConfig.getWsfev1Endpoint(),
                afipConfig.getWsfev1ServiceUrl());

        return WSFEV1Service.generarCAE(
                auth,
                sale,
                datosFact,
                ultimoComp,
                afipConfig.getWsfev1Endpoint(),
                afipConfig.getWsfev1ServiceUrl(),
                comprobanteAsociado,
                totalOverride);
    }

    private ContribuyenteData resolverReceptor(DatosFacturacion datosFact) throws ServerException {
        if (datosFact.getTipoComprobante().getTipo() == 'A') {
            ContribuyenteData receptor = consultarContribuyente(
                    tokenService.getTicket(Servicios.CONSTANCIA_INSCRIPCION),
                    datosFact.getNumeroDocumento());
            if (receptor.getCondicionIva() != CondicionIva.RESPONSABLE_INSCRIPTO) {
                throw new IllegalArgumentException(
                        "Error Fiscal: La Factura A solo puede emitirse a Responsables Inscriptos. "
                                + "El cliente actual es: " + receptor.getCondicionIva());
            }
            return receptor;
        }

        return ContribuyenteData.builder()
                .nombre("CONSUMIDOR FINAL")
                .direccion("S/D")
                .localidad("")
                .provincia("")
                .codigoPostal("")
                .condicionIva(CondicionIva.CONSUMIDOR_FINAL)
                .build();
    }

    private Factura obtenerFacturaOriginal(Long saleId) {
        return saleService.findFacturaBySaleId(saleId)
                .orElseThrow(() -> new RuntimeException("La venta no posee factura electronica para asociar."));
    }

    private static ContribuyenteData receptorDesdeComprobante(Comprobante comprobante) {
        return ContribuyenteData.builder()
                .nombre(comprobante.getRazonSocialCliente())
                .direccion(comprobante.getDomicilioCliente())
                .localidad("")
                .provincia("")
                .codigoPostal("")
                .condicionIva(comprobante.getCondicionIvaCliente())
                .build();
    }

    private static <T extends Comprobante> T mapearComprobante(T comprobante, Sale sale, DatosFacturacion datos,
            ContribuyenteData contribuyente, CAEResponse caeResponse, BigDecimal totalOverride) {

        comprobante.setCuitEmisor(datos.getCuitEmisor());
        comprobante.setPuntoVenta(datos.getPuntoVenta());
        comprobante.setTipoComprobante(datos.getTipoComprobante().getCodigo());
        comprobante.setFechaEmision(LocalDate.now());
        comprobante.setConcepto(datos.getConcepto());
        comprobante.setCuitCliente(datos.getNumeroDocumento());
        comprobante.setTipoDocCliente(datos.getTipoDocumento().getCodigo());
        comprobante.setRazonSocialCliente(contribuyente.getNombre());
        comprobante.setDomicilioCliente(contribuyente.getDomicilioComercialFormateado());
        comprobante.setCondicionIvaCliente(contribuyente.getCondicionIva());

        BigDecimal totalVenta = (totalOverride != null)
                ? totalOverride.setScale(2, RoundingMode.HALF_UP)
                : sale.getTotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal divisor = BigDecimal.ONE.add(datos.getAlicuota().getMultiplicador());
        BigDecimal neto = totalVenta.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal importeIva = totalVenta.subtract(neto);

        comprobante.setImpNeto(neto);
        comprobante.setImpIva(importeIva);
        comprobante.setImpTotal(totalVenta);

        if (caeResponse != null) {
            comprobante.setNumeroComprobante(caeResponse.getNumeroComprobante());
            comprobante.setCae(caeResponse.getCae());

            if (caeResponse.getFechaVencimiento() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                comprobante.setVtoCae(LocalDate.parse(caeResponse.getFechaVencimiento(), formatter));
            }

            comprobante.setObservaciones(caeResponse.getObservaciones());
            String resultado = (caeResponse.getCae() != null && !caeResponse.getCae().isEmpty()) ? "A" : "R";
            comprobante.setResultadoAfip(resultado);
        }

        if (datos.getConcepto() != Conceptos.PRODUCTOS) {
            comprobante.setFechaVencimientoPago(LocalDate.now().plusDays(30));
        } else {
            comprobante.setFechaVencimientoPago(null);
        }

        return comprobante;
    }

    private ContribuyenteData consultarContribuyente(AfipAuth auth, long idBuscado) throws ServerException {
        try {
            return AfipPadronClient.getPersonaV2(auth.getToken(), auth.getSign(), afipConfig.getCuit(), idBuscado,
                    afipConfig.getPadronEndpoint());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerException("Error al consultar el padron de AFIP");
        }
    }

    private String guardarPdfEnDisco(byte[] contenido, String nombreArchivo) throws Exception {
        try {
            String anio = String.valueOf(java.time.LocalDate.now().getYear());
            java.nio.file.Path directorioDestino = java.nio.file.Paths.get(RUTA_BASE_PDFS, anio);

            if (!java.nio.file.Files.exists(directorioDestino)) {
                java.nio.file.Files.createDirectories(directorioDestino);
            }

            java.nio.file.Path archivoDestino = directorioDestino.resolve(nombreArchivo);
            java.nio.file.Files.write(archivoDestino, contenido);

            return archivoDestino.toAbsolutePath().toString();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw new Exception("Error al intentar guardar el PDF en disco: " + e.getMessage());
        }
    }

    private String generarNombreArchivo(Comprobante comprobante) {
        TiposComprobante tipo = TiposComprobante.fromCodigo(comprobante.getTipoComprobante());
        String prefijo = tipo.isNotaCredito() ? "NC" : "F";
        return String.format("%s_%c_%05d-%08d.pdf",
                prefijo,
                tipo.getTipo(),
                comprobante.getPuntoVenta(),
                comprobante.getNumeroComprobante());
    }

    private String generarNumeroComprobante(Comprobante comprobante) {
        TiposComprobante tipo = TiposComprobante.fromCodigo(comprobante.getTipoComprobante());
        return String.format("%s %c %05d-%08d",
                tipo.isNotaCredito() ? "NC" : "F",
                tipo.getTipo(),
                comprobante.getPuntoVenta(),
                comprobante.getNumeroComprobante());
    }
}
