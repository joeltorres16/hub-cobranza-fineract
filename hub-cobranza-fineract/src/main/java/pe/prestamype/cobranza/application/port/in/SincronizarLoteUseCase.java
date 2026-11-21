package pe.prestamype.cobranza.application.port.in;

import pe.prestamype.cobranza.domain.model.LoteCobranza;

import java.time.LocalDate;
import java.util.List;

public interface SincronizarLoteUseCase {

    LoteCobranza sincronizar(ComandoSincronizacion comando);

    record ComandoSincronizacion(
            LocalDate fechaCorte,
            int diasHorizonte,
            List<Long> prestamoIds
    ) {
        public ComandoSincronizacion {
            if (fechaCorte == null) {
                throw new IllegalArgumentException("La fecha de corte es obligatoria");
            }
            if (diasHorizonte < 0) {
                throw new IllegalArgumentException("Los días de horizonte no pueden ser negativos");
            }
            if (prestamoIds == null || prestamoIds.isEmpty()) {
                throw new IllegalArgumentException("Debe indicar al menos un préstamo");
            }
        }
    }
}
