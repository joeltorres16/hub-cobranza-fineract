package pe.prestamype.cobranza.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class LoteCobranza {
    private Long id;
    private LocalDate fechaCorte;
    private int diasHorizonte;
    private LocalDate fechaFinHorizonte;
    private List<Long> prestamosSolicitados;
    private EstadoLote estado;
    private int totalOrdenes;
    private String mensajeError;
    private Instant creadoEn;
    private Instant actualizadoEn;

    public LoteCobranza(
            Long id,
            LocalDate fechaCorte,
            int diasHorizonte,
            List<Long> prestamosSolicitados,
            EstadoLote estado,
            int totalOrdenes,
            String mensajeError,
            Instant creadoEn,
            Instant actualizadoEn
    ) {
        if (diasHorizonte < 0) {
            throw new IllegalArgumentException("Los días de horizonte no pueden ser negativos");
        }
        this.id = id;
        this.fechaCorte = fechaCorte;
        this.diasHorizonte = diasHorizonte;
        this.fechaFinHorizonte = fechaCorte != null ? fechaCorte.plusDays(diasHorizonte) : null;
        this.prestamosSolicitados = prestamosSolicitados != null ? List.copyOf(prestamosSolicitados) : List.of();
        this.estado = estado;
        this.totalOrdenes = totalOrdenes;
        this.mensajeError = mensajeError;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public static LoteCobranza iniciar(
            LocalDate fechaCorte,
            int diasHorizonte,
            List<Long> prestamosSolicitados,
            Instant ahora
    ) {
        return new LoteCobranza(
                null,
                fechaCorte,
                diasHorizonte,
                prestamosSolicitados,
                EstadoLote.EN_PROCESO,
                0,
                null,
                ahora,
                ahora
        );
    }

    public void marcarCompletado(int totalOrdenes, Instant ahora) {
        this.totalOrdenes = totalOrdenes;
        this.estado = EstadoLote.COMPLETADO;
        this.mensajeError = null;
        this.actualizadoEn = ahora;
    }

    public void marcarCompletadoConErrores(int totalOrdenes, String mensajeError, Instant ahora) {
        this.totalOrdenes = totalOrdenes;
        this.estado = EstadoLote.COMPLETADO_CON_ERRORES;
        this.mensajeError = mensajeError;
        this.actualizadoEn = ahora;
    }

    public void marcarFallido(String mensajeError, Instant ahora) {
        this.estado = EstadoLote.FALLIDO;
        this.mensajeError = mensajeError;
        this.actualizadoEn = ahora;
    }

    public Long getId() { return id; }
    public LocalDate getFechaCorte() { return fechaCorte; }
    public int getDiasHorizonte() { return diasHorizonte; }
    public LocalDate getFechaFinHorizonte() { return fechaFinHorizonte; }
    public List<Long> getPrestamosSolicitados() { return prestamosSolicitados; }
    public EstadoLote getEstado() { return estado; }
    public int getTotalOrdenes() { return totalOrdenes; }
    public String getMensajeError() { return mensajeError; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
