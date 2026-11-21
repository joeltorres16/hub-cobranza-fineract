package pe.prestamype.cobranza.infrastructure.adapter.in.web.dto;

import pe.prestamype.cobranza.domain.model.OrdenCobranza;
import pe.prestamype.cobranza.domain.model.SituacionOrden;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record OrdenCobranzaResponse(
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
        BigDecimal totalPorCobrar,
        SituacionOrden situacion,
        Instant sincronizadoEn
) {
    public static OrdenCobranzaResponse fromDomain(OrdenCobranza orden) {
        return new OrdenCobranzaResponse(
                orden.getId(),
                orden.getLoteId(),
                orden.getClienteId(),
                orden.getClienteReferenciaExterna(),
                orden.getClienteNombre(),
                orden.getPrestamoId(),
                orden.getPrestamoReferenciaExterna(),
                orden.getNumeroCuota(),
                orden.getMoneda(),
                orden.getFechaVencimiento(),
                orden.getCapitalPendiente(),
                orden.getInteresPendiente(),
                orden.getTotalPorCobrar(),
                orden.getSituacion(),
                orden.getSincronizadoEn()
        );
    }
}
