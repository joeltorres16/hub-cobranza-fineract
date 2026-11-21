package pe.prestamype.cobranza.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import pe.prestamype.cobranza.domain.model.SituacionOrden;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "orden_cobranza")
public class OrdenCobranzaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lote_id", nullable = false)
    private Long loteId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "cliente_referencia_externa", length = 100)
    private String clienteReferenciaExterna;

    @Column(name = "cliente_nombre", length = 255)
    private String clienteNombre;

    @Column(name = "prestamo_id", nullable = false)
    private Long prestamoId;

    @Column(name = "prestamo_referencia_externa", length = 100)
    private String prestamoReferenciaExterna;

    @Column(name = "numero_cuota", nullable = false)
    private int numeroCuota;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "capital_pendiente", nullable = false, precision = 19, scale = 2)
    private BigDecimal capitalPendiente;

    @Column(name = "interes_pendiente", nullable = false, precision = 19, scale = 2)
    private BigDecimal interesPendiente;

    @Column(name = "total_por_cobrar", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPorCobrar;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacion", nullable = false, length = 30)
    private SituacionOrden situacion;

    @Column(name = "sincronizado_en", nullable = false)
    private Instant sincronizadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLoteId() { return loteId; }
    public void setLoteId(Long loteId) { this.loteId = loteId; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getClienteReferenciaExterna() { return clienteReferenciaExterna; }
    public void setClienteReferenciaExterna(String clienteReferenciaExterna) { this.clienteReferenciaExterna = clienteReferenciaExterna; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public Long getPrestamoId() { return prestamoId; }
    public void setPrestamoId(Long prestamoId) { this.prestamoId = prestamoId; }
    public String getPrestamoReferenciaExterna() { return prestamoReferenciaExterna; }
    public void setPrestamoReferenciaExterna(String prestamoReferenciaExterna) { this.prestamoReferenciaExterna = prestamoReferenciaExterna; }
    public int getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(int numeroCuota) { this.numeroCuota = numeroCuota; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public BigDecimal getCapitalPendiente() { return capitalPendiente; }
    public void setCapitalPendiente(BigDecimal capitalPendiente) { this.capitalPendiente = capitalPendiente; }
    public BigDecimal getInteresPendiente() { return interesPendiente; }
    public void setInteresPendiente(BigDecimal interesPendiente) { this.interesPendiente = interesPendiente; }
    public BigDecimal getTotalPorCobrar() { return totalPorCobrar; }
    public void setTotalPorCobrar(BigDecimal totalPorCobrar) { this.totalPorCobrar = totalPorCobrar; }
    public SituacionOrden getSituacion() { return situacion; }
    public void setSituacion(SituacionOrden situacion) { this.situacion = situacion; }
    public Instant getSincronizadoEn() { return sincronizadoEn; }
    public void setSincronizadoEn(Instant sincronizadoEn) { this.sincronizadoEn = sincronizadoEn; }
}
