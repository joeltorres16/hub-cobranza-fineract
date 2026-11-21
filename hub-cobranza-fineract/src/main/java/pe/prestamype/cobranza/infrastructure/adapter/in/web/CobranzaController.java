package pe.prestamype.cobranza.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.prestamype.cobranza.application.port.in.ConsultarCobranzaQuery;
import pe.prestamype.cobranza.application.port.in.SincronizarLoteUseCase;
import pe.prestamype.cobranza.domain.model.SituacionOrden;
import pe.prestamype.cobranza.infrastructure.adapter.in.web.dto.LoteCobranzaResponse;
import pe.prestamype.cobranza.infrastructure.adapter.in.web.dto.OrdenCobranzaResponse;
import pe.prestamype.cobranza.infrastructure.adapter.in.web.dto.SincronizarLoteRequest;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CobranzaController {

    private final SincronizarLoteUseCase sincronizarLoteUseCase;
    private final ConsultarCobranzaQuery consultarCobranzaQuery;

    public CobranzaController(
            SincronizarLoteUseCase sincronizarLoteUseCase,
            ConsultarCobranzaQuery consultarCobranzaQuery
    ) {
        this.sincronizarLoteUseCase = sincronizarLoteUseCase;
        this.consultarCobranzaQuery = consultarCobranzaQuery;
    }

    @PostMapping("/lotes/sincronizar")
    public ResponseEntity<LoteCobranzaResponse> sincronizarLote(@Valid @RequestBody SincronizarLoteRequest request) {
        var lote = sincronizarLoteUseCase.sincronizar(
                new SincronizarLoteUseCase.ComandoSincronizacion(
                        request.fechaCorte(),
                        request.diasHorizonte(),
                        request.prestamoIds()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(LoteCobranzaResponse.fromDomain(lote));
    }

    @GetMapping("/lotes/{loteId}")
    public ResponseEntity<LoteCobranzaResponse> buscarLote(@PathVariable Long loteId) {
        return consultarCobranzaQuery.buscarLotePorId(loteId)
                .map(LoteCobranzaResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/ordenes")
    public ResponseEntity<List<OrdenCobranzaResponse>> buscarOrdenes(
            @RequestParam(required = false) Long loteId,
            @RequestParam(required = false) Long prestamoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimientoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimientoHasta,
            @RequestParam(required = false) SituacionOrden situacion
    ) {
        List<OrdenCobranzaResponse> ordenes = consultarCobranzaQuery.buscarOrdenes(
                        new ConsultarCobranzaQuery.FiltroOrdenes(
                                loteId,
                                prestamoId,
                                fechaVencimientoDesde,
                                fechaVencimientoHasta,
                                situacion
                        )
                )
                .stream()
                .map(OrdenCobranzaResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(ordenes);
    }
}
