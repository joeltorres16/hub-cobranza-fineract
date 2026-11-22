package pe.prestamype.cobranza.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
        name = "Modulo de Cobranza Fineract",
        description = "Modulo integrado a Fineract, permite generar y consultar lotes de cobranza y sus ordenes. "
)
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

    @Operation(
            summary = "Sincronizar préstamos",
            description = """
            Consulta en Fineract el cronograma de cada préstamo indicado y crea un lote
            con órdenes locales para las cuotas que tengan saldo pendiente y venzan
            hasta el fin del horizonte. Incluye cuotas vencidas.
            """
    )
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

    @Operation(
            summary = "Consultar un lote local",
            description = """
        Devuelve el resultado de una ejecución de sincronización por su identificador.
        Si el lote no existe, responde con HTTP 404.
        """
    )
    @GetMapping("/lotes/{loteId}")
    public ResponseEntity<LoteCobranzaResponse> buscarLote(@PathVariable Long loteId) {
        return consultarCobranzaQuery.buscarLotePorId(loteId)
                .map(LoteCobranzaResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Consultar órdenes de cobranza",
            description = """
        Devuelve las órdenes locales de cobranza que coinciden con los filtros indicados.
        Se puede filtrar por lote, préstamo, rango de vencimiento y situación.
        Los filtros son opcionales. Si no hay coincidencias, devuelve una lista vacía.
        """
    )
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
