package pe.prestamype.cobranza.domain.service;

import pe.prestamype.cobranza.domain.model.CuotaCronograma;
import pe.prestamype.cobranza.domain.model.OrdenCobranza;
import pe.prestamype.cobranza.domain.model.PrestamoCronograma;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class ReglasSincronizacionService {

    public List<OrdenCobranza> generarOrdenesCandidatas(
            Long loteId,
            PrestamoCronograma prestamo,
            LocalDate fechaCorte,
            LocalDate fechaFinHorizonte,
            Instant sincronizadoEn
    ) {
        if (prestamo == null || prestamo.cuotas() == null || fechaFinHorizonte == null) {
            return List.of();
        }

        return prestamo.cuotas().stream()
                .filter(cuota -> esCuotaElegible(cuota, fechaFinHorizonte))
                .sorted(Comparator.comparingInt(CuotaCronograma::numeroCuota))
                .map(cuota -> OrdenCobranza.desdeCuota(loteId, prestamo, cuota, fechaCorte, sincronizadoEn))
                .toList();
    }

    private boolean esCuotaElegible(CuotaCronograma cuota, LocalDate fechaFinHorizonte) {
        if (cuota == null || cuota.fechaVencimiento() == null) {
            return false;
        }
        boolean dentroDeHorizonte = !cuota.fechaVencimiento().isAfter(fechaFinHorizonte);
        return dentroDeHorizonte && cuota.tieneSaldoPendiente();
    }
}
