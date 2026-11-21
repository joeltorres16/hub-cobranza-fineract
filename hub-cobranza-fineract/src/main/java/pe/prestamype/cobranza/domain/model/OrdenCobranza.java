package pe.prestamype.cobranza.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class OrdenCobranza {

    private Long id;
    private Long loteId;
    private Long clienteId;
    private String clienteReferenciaExterna;
    private String clienteNombre;
    private Long prestamoId;
    private String prestamoReferenciaExterna;
    private int numeroCuota;
    private String moneda;
    private LocalDate fechaVencimiento;
    private BigDecimal capitalPendiente;
    private BigDecimal interesPendiente;
    private BigDecimal totalPorCobrar;
    private SituacionOrden situacion;
    private Instant sincronizadoEn;

    public OrdenCobranza(
            Long id,
            Long loteId,
            Long clienteId,
            String clienteReferenciaExterna,
            String clienteNombre,
            Long prestamoId,
            String prestamoReferenciaExterna,
            int numeroCuota,
            String moneda,
            LocalDate fechaVencimiento,
            BigDecimal capitalPendiente,
            BigDecimal interesPendiente,
            SituacionOrden situacion,
            Instant sincronizadoEn
    ) {
        this.id = id;
        this.loteId = loteId;
        this.clienteId = clienteId;
        this.clienteReferenciaExterna = clienteReferenciaExterna;
        this.clienteNombre = clienteNombre;
        this.prestamoId = prestamoId;
        this.prestamoReferenciaExterna = prestamoReferenciaExterna;
        this.numeroCuota = numeroCuota;
        this.moneda = moneda;
        this.fechaVencimiento = fechaVencimiento;
        this.capitalPendiente = normalizarMonto(capitalPendiente);
        this.interesPendiente = normalizarMonto(interesPendiente);
        this.totalPorCobrar = this.capitalPendiente.add(this.interesPendiente);
        this.situacion = situacion;
        this.sincronizadoEn = sincronizadoEn;
    }

    public static OrdenCobranza desdeCuota(
            Long loteId,
            PrestamoCronograma prestamo,
            CuotaCronograma cuota,
            LocalDate fechaCorte,
            Instant sincronizadoEn
    ) {
        BigDecimal capPend = normalizarMonto(cuota.capitalPendiente());
        BigDecimal intPend = normalizarMonto(cuota.interesPendiente());
        SituacionOrden situacion = calcularSituacion(cuota, capPend, intPend, fechaCorte);

        return new OrdenCobranza(
                null,
                loteId,
                prestamo.clienteId(),
                prestamo.clienteReferenciaExterna(),
                prestamo.clienteNombre(),
                prestamo.prestamoId(),
                prestamo.prestamoReferenciaExterna(),
                cuota.numeroCuota(),
                prestamo.moneda(),
                cuota.fechaVencimiento(),
                capPend,
                intPend,
                situacion,
                sincronizadoEn
        );
    }

    public void actualizarDesdeCuota(CuotaCronograma cuota, LocalDate fechaCorte, Instant sincronizadoEn) {
        this.capitalPendiente = normalizarMonto(cuota.capitalPendiente());
        this.interesPendiente = normalizarMonto(cuota.interesPendiente());
        this.totalPorCobrar = this.capitalPendiente.add(this.interesPendiente);
        this.fechaVencimiento = cuota.fechaVencimiento();
        this.situacion = calcularSituacion(cuota, this.capitalPendiente, this.interesPendiente, fechaCorte);
        this.sincronizadoEn = sincronizadoEn;
    }

    private static SituacionOrden calcularSituacion(
            CuotaCronograma cuota,
            BigDecimal capPend,
            BigDecimal intPend,
            LocalDate fechaCorte
    ) {
        BigDecimal totalPend = capPend.add(intPend);
        if (totalPend.compareTo(BigDecimal.ZERO) <= 0 || cuota.pagadaEnFineract()) {
            return SituacionOrden.PAGADA;
        }
        boolean huboPagoParcial = (cuota.capitalPagado() != null && cuota.capitalPagado().compareTo(BigDecimal.ZERO) > 0)
                || (cuota.interesPagado() != null && cuota.interesPagado().compareTo(BigDecimal.ZERO) > 0);
        if (cuota.fechaVencimiento() != null && fechaCorte != null && cuota.fechaVencimiento().isBefore(fechaCorte)) {
            return SituacionOrden.VENCIDA;
        }
        if (huboPagoParcial) {
            return SituacionOrden.PAGO_PARCIAL;
        }
        return SituacionOrden.PENDIENTE;
    }

    private static BigDecimal normalizarMonto(BigDecimal monto) {
        return monto == null ? BigDecimal.ZERO : monto;
    }

    public Long getId() { return id; }
    public Long getLoteId() { return loteId; }
    public Long getClienteId() { return clienteId; }
    public String getClienteReferenciaExterna() { return clienteReferenciaExterna; }
    public String getClienteNombre() { return clienteNombre; }
    public Long getPrestamoId() { return prestamoId; }
    public String getPrestamoReferenciaExterna() { return prestamoReferenciaExterna; }
    public int getNumeroCuota() { return numeroCuota; }
    public String getMoneda() { return moneda; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public BigDecimal getCapitalPendiente() { return capitalPendiente; }
    public BigDecimal getInteresPendiente() { return interesPendiente; }
    public BigDecimal getTotalPorCobrar() { return totalPorCobrar; }
    public SituacionOrden getSituacion() { return situacion; }
    public Instant getSincronizadoEn() { return sincronizadoEn; }
}
