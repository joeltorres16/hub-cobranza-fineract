package pe.prestamype.cobranza.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;
import pe.prestamype.cobranza.application.port.out.LoteCobranzaRepositoryPort;
import pe.prestamype.cobranza.domain.model.LoteCobranza;
import pe.prestamype.cobranza.infrastructure.adapter.out.persistence.entity.LoteCobranzaEntity;
import pe.prestamype.cobranza.infrastructure.adapter.out.persistence.repository.JpaLoteCobranzaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LoteCobranzaPersistenceAdapter implements LoteCobranzaRepositoryPort {

    private final JpaLoteCobranzaRepository repository;

    public LoteCobranzaPersistenceAdapter(JpaLoteCobranzaRepository repository) {
        this.repository = repository;
    }

    @Override
    public LoteCobranza guardar(LoteCobranza lote) {
        LoteCobranzaEntity entity = toEntity(lote);
        LoteCobranzaEntity guardada = repository.save(entity);
        return toDomain(guardada);
    }

    @Override
    public Optional<LoteCobranza> buscarPorId(Long loteId) {
        return repository.findById(loteId).map(this::toDomain);
    }

    private LoteCobranzaEntity toEntity(LoteCobranza lote) {
        LoteCobranzaEntity entity = new LoteCobranzaEntity();
        entity.setId(lote.getId());
        entity.setFechaCorte(lote.getFechaCorte());
        entity.setDiasHorizonte(lote.getDiasHorizonte());
        entity.setFechaFinHorizonte(lote.getFechaFinHorizonte());
        entity.setPrestamosSolicitados(serializarIds(lote.getPrestamosSolicitados()));
        entity.setEstado(lote.getEstado());
        entity.setTotalOrdenes(lote.getTotalOrdenes());
        entity.setMensajeError(lote.getMensajeError());
        entity.setCreadoEn(lote.getCreadoEn());
        entity.setActualizadoEn(lote.getActualizadoEn());
        return entity;
    }

    private LoteCobranza toDomain(LoteCobranzaEntity entity) {
        return new LoteCobranza(
                entity.getId(),
                entity.getFechaCorte(),
                entity.getDiasHorizonte(),
                deserializarIds(entity.getPrestamosSolicitados()),
                entity.getEstado(),
                entity.getTotalOrdenes(),
                entity.getMensajeError(),
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }

    private String serializarIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Long> deserializarIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}
