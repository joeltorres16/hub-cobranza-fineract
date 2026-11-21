package pe.prestamype.cobranza.application.port.out;

import pe.prestamype.cobranza.application.port.in.ConsultarCobranzaQuery;
import pe.prestamype.cobranza.domain.model.OrdenCobranza;

import java.util.List;
import java.util.Optional;

public interface OrdenCobranzaRepositoryPort {

    OrdenCobranza guardar(OrdenCobranza orden);

    List<OrdenCobranza> guardarTodos(List<OrdenCobranza> ordenes);

    Optional<OrdenCobranza> buscarPorLotePrestamoYCuota(Long loteId, Long prestamoId, int numeroCuota);

    List<OrdenCobranza> buscarPorFiltro(ConsultarCobranzaQuery.FiltroOrdenes filtro);
}
