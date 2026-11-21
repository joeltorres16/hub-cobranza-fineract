package pe.prestamype.cobranza.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pe.prestamype.cobranza.infrastructure.adapter.out.persistence.entity.OrdenCobranzaEntity;

import java.util.List;
import java.util.Optional;

public interface JpaOrdenCobranzaRepository extends JpaRepository<OrdenCobranzaEntity, Long>, JpaSpecificationExecutor<OrdenCobranzaEntity> {

    Optional<OrdenCobranzaEntity> findByLoteIdAndPrestamoIdAndNumeroCuota( Long loteId, Long prestamoId, int numeroCuota);

    List<OrdenCobranzaEntity> findByLoteId(Long loteId);
}
