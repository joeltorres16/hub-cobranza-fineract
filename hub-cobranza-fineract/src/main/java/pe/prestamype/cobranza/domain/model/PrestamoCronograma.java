package pe.prestamype.cobranza.domain.model;

import java.util.List;

public record PrestamoCronograma(
        Long prestamoId,
        String prestamoReferenciaExterna,
        Long clienteId,
        String clienteReferenciaExterna,
        String clienteNombre,
        String moneda,
        List<CuotaCronograma> cuotas
) {
}
