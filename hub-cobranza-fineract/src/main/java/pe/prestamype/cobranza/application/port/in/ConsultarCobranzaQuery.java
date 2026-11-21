package pe.prestamype.cobranza.application.port.in;

import pe.prestamype.cobranza.domain.model.LoteCobranza;
import pe.prestamype.cobranza.domain.model.OrdenCobranza;
import pe.prestamype.cobranza.domain.model.SituacionOrden;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsultarCobranzaQuery {

    Optional<LoteCobranza> buscarLotePorId(Long loteId);
    List<OrdenCobranza> buscarOrdenes(FiltroOrdenes filtro);

    record FiltroOrdenes(
            Long loteId,
            Long prestamoId,
            LocalDate fechaVencimientoDesde,
            LocalDate fechaVencimientoHasta,
            SituacionOrden situacion
    ) {
    }
}
