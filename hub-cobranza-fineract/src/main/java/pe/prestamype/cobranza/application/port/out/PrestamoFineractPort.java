package pe.prestamype.cobranza.application.port.out;

import pe.prestamype.cobranza.domain.model.PrestamoCronograma;

import java.util.Optional;

public interface PrestamoFineractPort {

    Optional<PrestamoCronograma> obtenerPrestamoConCronograma(Long prestamoId);
}
