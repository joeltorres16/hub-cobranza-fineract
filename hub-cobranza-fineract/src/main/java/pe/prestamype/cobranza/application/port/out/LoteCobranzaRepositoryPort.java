package pe.prestamype.cobranza.application.port.out;

import pe.prestamype.cobranza.domain.model.LoteCobranza;

import java.util.Optional;

public interface LoteCobranzaRepositoryPort {

    LoteCobranza guardar(LoteCobranza lote);

    Optional<LoteCobranza> buscarPorId(Long loteId);
}
