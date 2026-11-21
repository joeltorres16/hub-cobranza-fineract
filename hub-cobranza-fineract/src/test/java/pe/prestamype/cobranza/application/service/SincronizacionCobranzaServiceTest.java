package pe.prestamype.cobranza.application.service;

import org.junit.jupiter.api.Test;
import pe.prestamype.cobranza.application.port.in.ConsultarCobranzaQuery;
import pe.prestamype.cobranza.application.port.in.SincronizarLoteUseCase;
import pe.prestamype.cobranza.application.port.out.LoteCobranzaRepositoryPort;
import pe.prestamype.cobranza.application.port.out.OrdenCobranzaRepositoryPort;
import pe.prestamype.cobranza.application.port.out.PrestamoFineractPort;
import pe.prestamype.cobranza.domain.model.*;
import pe.prestamype.cobranza.domain.service.ReglasSincronizacionService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SincronizacionCobranzaServiceTest {

    @Test
    void debeGenerarOrdenesSoloParaCuotasPendientesDentroDelHorizonte() {
        Clock clockFijo = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneOffset.UTC);

        CuotaCronograma cuotaVencida = new CuotaCronograma(
                1,
                LocalDate.of(2026, 10, 1),
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                new BigDecimal("500.00"),
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("50.00"),
                false
        );

        CuotaCronograma cuotaDentroHorizonte = new CuotaCronograma(
                2,
                LocalDate.of(2026, 11, 10),
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                new BigDecimal("500.00"),
                new BigDecimal("40.00"),
                BigDecimal.ZERO,
                new BigDecimal("40.00"),
                false
        );

        CuotaCronograma cuotaFueraHorizonte = new CuotaCronograma(
                3,
                LocalDate.of(2026, 12, 15),
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                new BigDecimal("500.00"),
                new BigDecimal("30.00"),
                BigDecimal.ZERO,
                new BigDecimal("30.00"),
                false
        );

        PrestamoCronograma prestamo = new PrestamoCronograma(
                101L,
                "LOAN-DEMO-001",
                202L,
                "CLI-001",
                "Juan Perez",
                "PEN",
                List.of(cuotaVencida, cuotaDentroHorizonte, cuotaFueraHorizonte)
        );

        PrestamoFineractPort fineractPort = id -> id.equals(101L) ? Optional.of(prestamo) : Optional.empty();
        InMemoryLoteRepository loteRepo = new InMemoryLoteRepository();
        InMemoryOrdenRepository ordenRepo = new InMemoryOrdenRepository();

        SincronizacionCobranzaService service = new SincronizacionCobranzaService(
                fineractPort,
                loteRepo,
                ordenRepo,
                new ReglasSincronizacionService(),
                clockFijo
        );

        LoteCobranza resultado = service.sincronizar(
                new SincronizarLoteUseCase.ComandoSincronizacion(LocalDate.of(2026, 10, 15), 31, List.of(101L))
        );

        assertNotNull(resultado.getId());
        assertEquals(EstadoLote.COMPLETADO, resultado.getEstado());
        assertEquals(LocalDate.of(2026, 11, 15), resultado.getFechaFinHorizonte());
        assertEquals(2, resultado.getTotalOrdenes());
        assertEquals(2, ordenRepo.ordenes.size());
    }

    private static class InMemoryLoteRepository implements LoteCobranzaRepositoryPort {
        private final Map<Long, LoteCobranza> store = new HashMap<>();
        private long seq = 1L;

        @Override
        public LoteCobranza guardar(LoteCobranza lote) {
            if (lote.getId() == null) {
                LoteCobranza creado = new LoteCobranza(
                        seq++,
                        lote.getFechaCorte(),
                        lote.getDiasHorizonte(),
                        lote.getPrestamosSolicitados(),
                        lote.getEstado(),
                        lote.getTotalOrdenes(),
                        lote.getMensajeError(),
                        lote.getCreadoEn(),
                        lote.getActualizadoEn()
                );
                store.put(creado.getId(), creado);
                return creado;
            }
            store.put(lote.getId(), lote);
            return lote;
        }

        @Override
        public Optional<LoteCobranza> buscarPorId(Long loteId) {
            return Optional.ofNullable(store.get(loteId));
        }
    }

    private static class InMemoryOrdenRepository implements OrdenCobranzaRepositoryPort {
        private final List<OrdenCobranza> ordenes = new ArrayList<>();
        private long seq = 1L;

        @Override
        public OrdenCobranza guardar(OrdenCobranza orden) {
            ordenes.removeIf(o -> o.getLoteId().equals(orden.getLoteId())
                    && o.getPrestamoId().equals(orden.getPrestamoId())
                    && o.getNumeroCuota() == orden.getNumeroCuota());
            OrdenCobranza guardada = new OrdenCobranza(
                    orden.getId() != null ? orden.getId() : seq++,
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
                    orden.getSituacion(),
                    orden.getSincronizadoEn()
            );
            ordenes.add(guardada);
            return guardada;
        }

        @Override
        public List<OrdenCobranza> guardarTodos(List<OrdenCobranza> nuevas) {
            List<OrdenCobranza> res = new ArrayList<>();
            for (OrdenCobranza o : nuevas) {
                res.add(guardar(o));
            }
            return res;
        }

        @Override
        public Optional<OrdenCobranza> buscarPorLotePrestamoYCuota(Long loteId, Long prestamoId, int numeroCuota) {
            return ordenes.stream()
                    .filter(o -> o.getLoteId().equals(loteId)
                            && o.getPrestamoId().equals(prestamoId)
                            && o.getNumeroCuota() == numeroCuota)
                    .findFirst();
        }

        @Override
        public List<OrdenCobranza> buscarPorFiltro(ConsultarCobranzaQuery.FiltroOrdenes filtro) {
            return ordenes.stream()
                    .filter(o -> filtro.loteId() == null || o.getLoteId().equals(filtro.loteId()))
                    .filter(o -> filtro.prestamoId() == null || o.getPrestamoId().equals(filtro.prestamoId()))
                    .toList();
        }
    }
}