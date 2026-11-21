package pe.prestamype.cobranza.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import pe.prestamype.cobranza.domain.model.EstadoLote;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "lote_cobranza")
public class LoteCobranzaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(name = "dias_horizonte", nullable = false)
    private int diasHorizonte;

    @Column(name = "fecha_fin_horizonte", nullable = false)
    private LocalDate fechaFinHorizonte;

    @Column(name = "prestamos_solicitados", columnDefinition = "TEXT")
    private String prestamosSolicitados;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoLote estado;

    @Column(name = "total_ordenes", nullable = false)
    private int totalOrdenes;

    @Column(name = "mensaje_error", length = 1000)
    private String mensajeError;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @PrePersist
    protected void onCreate() {
        Instant ahora = Instant.now();
        if (creadoEn == null) {
            creadoEn = ahora;
        }
        if (actualizadoEn == null) {
            actualizadoEn = ahora;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(LocalDate fechaCorte) { this.fechaCorte = fechaCorte; }
    public int getDiasHorizonte() { return diasHorizonte; }
    public void setDiasHorizonte(int diasHorizonte) { this.diasHorizonte = diasHorizonte; }
    public LocalDate getFechaFinHorizonte() { return fechaFinHorizonte; }
    public void setFechaFinHorizonte(LocalDate fechaFinHorizonte) { this.fechaFinHorizonte = fechaFinHorizonte; }
    public String getPrestamosSolicitados() { return prestamosSolicitados; }
    public void setPrestamosSolicitados(String prestamosSolicitados) { this.prestamosSolicitados = prestamosSolicitados; }
    public EstadoLote getEstado() { return estado; }
    public void setEstado(EstadoLote estado) { this.estado = estado; }
    public int getTotalOrdenes() { return totalOrdenes; }
    public void setTotalOrdenes(int totalOrdenes) { this.totalOrdenes = totalOrdenes; }
    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
