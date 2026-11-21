package pe.prestamype.cobranza.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.prestamype.cobranza.infrastructure.adapter.in.web.dto.ApiErrorResponse;

import java.time.Instant;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {

    public GlobalExceptionHandler() {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        ApiErrorResponse errorBody = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .distinct()
                .collect(Collectors.joining(", "));

        ApiErrorResponse errorBody = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Campos inválidos: " + detalles
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIntegrationError(IllegalStateException ex) {
        ApiErrorResponse errorBody = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody);
    }
}
