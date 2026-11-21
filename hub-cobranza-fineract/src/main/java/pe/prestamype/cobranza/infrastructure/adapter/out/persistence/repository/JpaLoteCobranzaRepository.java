package pe.prestamype.cobranza.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.prestamype.cobranza.infrastructure.adapter.out.persistence.entity.LoteCobranzaEntity;

public interface JpaLoteCobranzaRepository extends JpaRepository<LoteCobranzaEntity, Long> {
}
