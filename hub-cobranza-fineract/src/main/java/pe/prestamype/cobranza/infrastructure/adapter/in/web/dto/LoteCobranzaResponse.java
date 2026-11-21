package pe.prestamype.cobranza.infrastructure.adapter.in.web.dto;

import pe.prestamype.cobranza.domain.model.EstadoLote;
import pe.prestamype.cobranza.domain.model.LoteCobranza;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record LoteCobranzaResponse(
        Long id,
        LocalDate fechaCorte,
        int diasHorizonte,
        LocalDate fechaFinHorizonte,
        List<Long> prestamosSolicitados,
        EstadoLote estado,
        int totalOrdenes,
        String mensajeError,
        Instant creadoEn,
        Instant actualizadoEn
) {
    public static LoteCobranzaResponse fromDomain(LoteCobranza lote) {
        return new LoteCobranzaResponse(
                lote.getId(),
                lote.getFechaCorte(),
                lote.getDiasHorizonte(),
                lote.getFechaFinHorizonte(),
                lote.getPrestamosSolicitados(),
                lote.getEstado(),
                lote.getTotalOrdenes(),
                lote.getMensajeError(),
                lote.getCreadoEn(),
                lote.getActualizadoEn()
        );
    }
}
