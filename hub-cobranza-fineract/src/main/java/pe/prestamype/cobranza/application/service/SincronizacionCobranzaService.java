package pe.prestamype.cobranza.application.service;

import pe.prestamype.cobranza.application.port.in.ConsultarCobranzaQuery;
import pe.prestamype.cobranza.application.port.in.SincronizarLoteUseCase;
import pe.prestamype.cobranza.application.port.out.LoteCobranzaRepositoryPort;
import pe.prestamype.cobranza.application.port.out.OrdenCobranzaRepositoryPort;
import pe.prestamype.cobranza.application.port.out.PrestamoFineractPort;
import pe.prestamype.cobranza.domain.model.LoteCobranza;
import pe.prestamype.cobranza.domain.model.OrdenCobranza;
import pe.prestamype.cobranza.domain.model.PrestamoCronograma;
import pe.prestamype.cobranza.domain.service.ReglasSincronizacionService;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SincronizacionCobranzaService implements SincronizarLoteUseCase, ConsultarCobranzaQuery {

    private final PrestamoFineractPort prestamoFineractPort;
    private final LoteCobranzaRepositoryPort loteRepositoryPort;
    private final OrdenCobranzaRepositoryPort ordenRepositoryPort;
    private final ReglasSincronizacionService reglasSincronizacionService;
    private final Clock clock;

    public SincronizacionCobranzaService(
            PrestamoFineractPort prestamoFineractPort,
            LoteCobranzaRepositoryPort loteRepositoryPort,
            OrdenCobranzaRepositoryPort ordenRepositoryPort,
            ReglasSincronizacionService reglasSincronizacionService,
            Clock clock
    ) {
        this.prestamoFineractPort = prestamoFineractPort;
        this.loteRepositoryPort = loteRepositoryPort;
        this.ordenRepositoryPort = ordenRepositoryPort;
        this.reglasSincronizacionService = reglasSincronizacionService;
        this.clock = clock;
    }

    @Override
    public LoteCobranza sincronizar(ComandoSincronizacion comando) {
        Instant ahora = Instant.now(clock);
        LoteCobranza lote = LoteCobranza.iniciar(
                comando.fechaCorte(),
                comando.diasHorizonte(),
                comando.prestamoIds(),
                ahora
        );
        lote = loteRepositoryPort.guardar(lote);

        LocalDate fechaCorte = lote.getFechaCorte();
        LocalDate fechaFinHorizonte = lote.getFechaFinHorizonte();

        List<OrdenCobranza> ordenesAGuardar = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (Long prestamoId : comando.prestamoIds()) {
            try {
                Optional<PrestamoCronograma> prestamoOpt = prestamoFineractPort.obtenerPrestamoConCronograma(prestamoId);
                if (prestamoOpt.isEmpty()) {
                    errores.add("Préstamo no encontrado en Fineract: " + prestamoId);
                    continue;
                }

                List<OrdenCobranza> candidatas = reglasSincronizacionService.generarOrdenesCandidatas(
                        lote.getId(),
                        prestamoOpt.get(),
                        fechaCorte,
                        fechaFinHorizonte,
                        Instant.now(clock)
                );

                for (OrdenCobranza candidata : candidatas) {
                    Optional<OrdenCobranza> existenteOpt = ordenRepositoryPort.buscarPorLotePrestamoYCuota(
                            lote.getId(),
                            candidata.getPrestamoId(),
                            candidata.getNumeroCuota()
                    );
                    if (existenteOpt.isPresent()) {
                        OrdenCobranza existente = existenteOpt.get();
                        ordenRepositoryPort.guardar(new OrdenCobranza(
                                existente.getId(),
                                existente.getLoteId(),
                                candidata.getClienteId(),
                                candidata.getClienteReferenciaExterna(),
                                candidata.getClienteNombre(),
                                candidata.getPrestamoId(),
                                candidata.getPrestamoReferenciaExterna(),
                                candidata.getNumeroCuota(),
                                candidata.getMoneda(),
                                candidata.getFechaVencimiento(),
                                candidata.getCapitalPendiente(),
                                candidata.getInteresPendiente(),
                                candidata.getSituacion(),
                                candidata.getSincronizadoEn()
                        ));
                    } else {
                        ordenesAGuardar.add(candidata);
                    }
                }
            } catch (Exception ex) {
                errores.add("Error sincronizando préstamo " + prestamoId + ": " + ex.getMessage());
            }
        }

        if (!ordenesAGuardar.isEmpty()) {
            ordenRepositoryPort.guardarTodos(ordenesAGuardar);
        }

        List<OrdenCobranza> totalDelLote = ordenRepositoryPort.buscarPorFiltro(
                new FiltroOrdenes(lote.getId(), null, null, null, null)
        );
        int totalOrdenes = totalDelLote.size();

        Instant fin = Instant.now(clock);
        if (errores.isEmpty()) {
            lote.marcarCompletado(totalOrdenes, fin);
        } else if (totalOrdenes > 0) {
            lote.marcarCompletadoConErrores(totalOrdenes, String.join(" | ", errores), fin);
        } else {
            lote.marcarFallido(String.join(" | ", errores), fin);
        }

        return loteRepositoryPort.guardar(lote);
    }

    @Override
    public Optional<LoteCobranza> buscarLotePorId(Long loteId) {
        return loteRepositoryPort.buscarPorId(loteId);
    }

    @Override
    public List<OrdenCobranza> buscarOrdenes(FiltroOrdenes filtro) {
        return ordenRepositoryPort.buscarPorFiltro(filtro);
    }
}
