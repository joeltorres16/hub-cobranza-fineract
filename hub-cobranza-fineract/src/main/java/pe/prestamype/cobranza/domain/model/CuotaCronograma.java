package pe.prestamype.cobranza.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaCronograma(
        int numeroCuota,
        LocalDate fechaVencimiento,
        BigDecimal capitalOriginal,
        BigDecimal capitalPagado,
        BigDecimal capitalPendiente,
        BigDecimal interesOriginal,
        BigDecimal interesPagado,
        BigDecimal interesPendiente,
        boolean pagadaEnFineract
) {
    public BigDecimal totalPendiente() {
        BigDecimal cap = capitalPendiente != null ? capitalPendiente : BigDecimal.ZERO;
        BigDecimal intPend = interesPendiente != null ? interesPendiente : BigDecimal.ZERO;
        return cap.add(intPend);
    }

    public boolean tieneSaldoPendiente() {
        return totalPendiente().compareTo(BigDecimal.ZERO) > 0;
    }
}
