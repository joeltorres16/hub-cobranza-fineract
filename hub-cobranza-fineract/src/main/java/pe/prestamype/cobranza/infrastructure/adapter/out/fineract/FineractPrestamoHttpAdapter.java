package pe.prestamype.cobranza.infrastructure.adapter.out.fineract;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.prestamype.cobranza.application.port.out.PrestamoFineractPort;
import pe.prestamype.cobranza.domain.model.CuotaCronograma;
import pe.prestamype.cobranza.domain.model.PrestamoCronograma;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Component
public class FineractPrestamoHttpAdapter implements PrestamoFineractPort {

    private final RestClient fineractRestClient;

    public FineractPrestamoHttpAdapter(RestClient fineractRestClient) {
        this.fineractRestClient = fineractRestClient;
    }

    @Override
    public Optional<PrestamoCronograma> obtenerPrestamoConCronograma(Long prestamoId) {
        try {
            JsonNode root = fineractRestClient.get()
                    .uri("/loans/{loanId}?associations=repaymentSchedule", prestamoId)
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null || root.isMissingNode() || root.isNull()) {
                return Optional.empty();
            }

            Long idPrestamo = root.path("id").isNumber() ? root.path("id").asLong() : prestamoId;
            String refExternaPrestamo = textoOpcional(root, "externalId");
            if (refExternaPrestamo == null) {
                refExternaPrestamo = textoOpcional(root, "accountNo");
            }

            Long clienteId = root.path("clientId").isNumber() ? root.path("clientId").asLong() : null;
            String refExternaCliente = textoOpcional(root, "clientExternalId");
            String clienteNombre = textoOpcional(root, "clientName");
            if (clienteNombre == null || clienteNombre.isBlank()) {
                clienteNombre = "Cliente #" + (clienteId != null ? clienteId : idPrestamo);
            }

            String moneda = root.path("currency").path("code").asText("PEN");

            List<CuotaCronograma> cuotas = extraerCuotas(root.path("repaymentSchedule").path("periods"));

            return Optional.of(new PrestamoCronograma(
                    idPrestamo,
                    refExternaPrestamo,
                    clienteId,
                    refExternaCliente,
                    clienteNombre,
                    moneda,
                    cuotas
            ));
        } catch (RestClientResponseException ex) {
            HttpStatusCode status = ex.getStatusCode();
            if (status.value() == 404) {
                return Optional.empty();
            }
            throw new IllegalStateException(
                    "Error HTTP al consultar préstamo " + prestamoId + " en Fineract (" + status.value() + "): "
                            + ex.getResponseBodyAsString(),
                    ex
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Error al obtener préstamo y cronograma desde Fineract para loanId=" + prestamoId,
                    ex
            );
        }
    }

    private List<CuotaCronograma> extraerCuotas(JsonNode periodsNode) {
        if (periodsNode == null || !periodsNode.isArray()) {
            return List.of();
        }

        List<CuotaCronograma> resultado = new ArrayList<>();
        for (JsonNode period : periodsNode) {
            if (!period.has("period") || !period.path("period").isInt()) {
                continue;
            }
            int numeroCuota = period.path("period").asInt();
            LocalDate fechaVencimiento = parseFecha(period.path("dueDate"));
            if (fechaVencimiento == null) {
                continue;
            }

            BigDecimal capitalOriginal = decimalOZero(period, "principalLoanBalanceOutstanding")
                    .compareTo(BigDecimal.ZERO) > 0 || period.has("principalOriginalDue")
                    ? decimalOZero(period, "principalOriginalDue")
                    : decimalOZero(period, "principalDue");
            BigDecimal capitalPagado = decimalOZero(period, "principalPaid");
            BigDecimal capitalPendiente = period.has("principalOutstanding")
                    ? decimalOZero(period, "principalOutstanding")
                    : capitalOriginal.subtract(capitalPagado).max(BigDecimal.ZERO);

            BigDecimal interesOriginal = period.has("interestOriginalDue")
                    ? decimalOZero(period, "interestOriginalDue")
                    : decimalOZero(period, "interestDue");
            BigDecimal interesPagado = decimalOZero(period, "interestPaid");
            BigDecimal interesPendiente = period.has("interestOutstanding")
                    ? decimalOZero(period, "interestOutstanding")
                    : interesOriginal.subtract(interesPagado).max(BigDecimal.ZERO);

            boolean pagadaEnFineract = period.path("complete").asBoolean(false);

            resultado.add(new CuotaCronograma(
                    numeroCuota,
                    fechaVencimiento,
                    capitalOriginal,
                    capitalPagado,
                    capitalPendiente,
                    interesOriginal,
                    interesPagado,
                    interesPendiente,
                    pagadaEnFineract
            ));
        }
        return resultado;
    }

    private BigDecimal decimalOZero(JsonNode node, String field) {
        JsonNode valueNode = node.path(field);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return BigDecimal.ZERO;
        }
        if (valueNode.isNumber()) {
            return valueNode.decimalValue();
        }
        String text = valueNode.asText("").trim();
        if (text.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(text);
    }

    private LocalDate parseFecha(JsonNode dateNode) {
        if (dateNode == null || dateNode.isMissingNode() || dateNode.isNull()) {
            return null;
        }
        // se recibe en formato [2026, 10, 15]
        if (dateNode.isArray() && dateNode.size() >= 3) {
            int year = dateNode.get(0).asInt();
            int month = dateNode.get(1).asInt();
            int day = dateNode.get(2).asInt();
            return LocalDate.of(year, month, day);
        }
        // O en formato texto ISO "2026-10-15"
        if (dateNode.isTextual()) {
            String text = dateNode.asText().trim();
            if (!text.isEmpty()) {
                return LocalDate.parse(text);
            }
        }
        return null;
    }

    private String textoOpcional(JsonNode node, String field) {
        JsonNode valueNode = node.path(field);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        String text = valueNode.asText().trim();
        return text.isEmpty() ? null : text;
    }
}
