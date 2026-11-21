package pe.prestamype.cobranza.infrastructure.adapter.out.persistence;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import pe.prestamype.cobranza.application.port.in.ConsultarCobranzaQuery;
import pe.prestamype.cobranza.application.port.out.OrdenCobranzaRepositoryPort;
import pe.prestamype.cobranza.domain.model.OrdenCobranza;
import pe.prestamype.cobranza.infrastructure.adapter.out.persistence.entity.OrdenCobranzaEntity;
import pe.prestamype.cobranza.infrastructure.adapter.out.persistence.repository.JpaOrdenCobranzaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class OrdenCobranzaPersistenceAdapter implements OrdenCobranzaRepositoryPort {

    private final JpaOrdenCobranzaRepository repository;

    public OrdenCobranzaPersistenceAdapter(JpaOrdenCobranzaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrdenCobranza guardar(OrdenCobranza orden) {
        OrdenCobranzaEntity guardada = repository.save(toEntity(orden));
        return toDomain(guardada);
    }

    @Override
    public List<OrdenCobranza> guardarTodos(List<OrdenCobranza> ordenes) {
        if (ordenes == null || ordenes.isEmpty()) {
            return List.of();
        }
        List<OrdenCobranzaEntity> entidades = ordenes.stream().map(this::toEntity).toList();
        return repository.saveAll(entidades).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<OrdenCobranza> buscarPorLotePrestamoYCuota(Long loteId, Long prestamoId, int numeroCuota) {
        return repository.findByLoteIdAndPrestamoIdAndNumeroCuota(loteId, prestamoId, numeroCuota)
                .map(this::toDomain);
    }

    @Override
    public List<OrdenCobranza> buscarPorFiltro(ConsultarCobranzaQuery.FiltroOrdenes filtro) {
        Specification<OrdenCobranzaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filtro != null) {
                if (filtro.loteId() != null) {
                    predicates.add(cb.equal(root.get("loteId"), filtro.loteId()));
                }
                if (filtro.prestamoId() != null) {
                    predicates.add(cb.equal(root.get("prestamoId"), filtro.prestamoId()));
                }
                if (filtro.fechaVencimientoDesde() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("fechaVencimiento"), filtro.fechaVencimientoDesde()));
                }
                if (filtro.fechaVencimientoHasta() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("fechaVencimiento"), filtro.fechaVencimientoHasta()));
                }
                if (filtro.situacion() != null) {
                    predicates.add(cb.equal(root.get("situacion"), filtro.situacion()));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec).stream().map(this::toDomain).toList();
    }

    private OrdenCobranzaEntity toEntity(OrdenCobranza orden) {
        OrdenCobranzaEntity entity = new OrdenCobranzaEntity();
        entity.setId(orden.getId());
        entity.setLoteId(orden.getLoteId());
        entity.setClienteId(orden.getClienteId());
        entity.setClienteReferenciaExterna(orden.getClienteReferenciaExterna());
        entity.setClienteNombre(orden.getClienteNombre());
        entity.setPrestamoId(orden.getPrestamoId());
        entity.setPrestamoReferenciaExterna(orden.getPrestamoReferenciaExterna());
        entity.setNumeroCuota(orden.getNumeroCuota());
        entity.setMoneda(orden.getMoneda());
        entity.setFechaVencimiento(orden.getFechaVencimiento());
        entity.setCapitalPendiente(orden.getCapitalPendiente());
        entity.setInteresPendiente(orden.getInteresPendiente());
        entity.setTotalPorCobrar(orden.getTotalPorCobrar());
        entity.setSituacion(orden.getSituacion());
        entity.setSincronizadoEn(orden.getSincronizadoEn());
        return entity;
    }

    private OrdenCobranza toDomain(OrdenCobranzaEntity entity) {
        return new OrdenCobranza(
                entity.getId(),
                entity.getLoteId(),
                entity.getClienteId(),
                entity.getClienteReferenciaExterna(),
                entity.getClienteNombre(),
                entity.getPrestamoId(),
                entity.getPrestamoReferenciaExterna(),
                entity.getNumeroCuota(),
                entity.getMoneda(),
                entity.getFechaVencimiento(),
                entity.getCapitalPendiente(),
                entity.getInteresPendiente(),
                entity.getSituacion(),
                entity.getSincronizadoEn()
        );
    }
}
