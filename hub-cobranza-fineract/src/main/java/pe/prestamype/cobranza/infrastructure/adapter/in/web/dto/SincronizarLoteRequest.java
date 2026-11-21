package pe.prestamype.cobranza.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SincronizarLoteRequest(
        @NotNull(message = "La fecha de corte es obligatoria")
        LocalDate fechaCorte,
        @Min(value = 0, message = "Los días de horizonte no pueden ser negativos")
        int diasHorizonte,
        @NotEmpty(message = "Debe enviar al menos un ID de préstamo")
        List<@NotNull Long> prestamoIds
) {
}
