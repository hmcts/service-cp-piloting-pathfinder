package uk.gov.hmcts.cp.controllers;

import io.micrometer.tracing.Tracer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.openapi.model.ErrorResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Tracer tracer;

    public GlobalExceptionHandler(final Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(final ResponseStatusException responseStatusException) {
        final ErrorResponse error = ErrorResponse.builder()
                .error(String.valueOf(responseStatusException.getStatusCode().value()))
                .message(responseStatusException.getReason() != null ? responseStatusException.getReason() : responseStatusException.getMessage())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .traceId(Objects.requireNonNull(tracer.currentSpan()).context().traceId())
                .build();

        return ResponseEntity
                .status(responseStatusException.getStatusCode())
                .body(error);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(final HttpClientErrorException httpClientErrorException) {
        // Extract just the custom message, not the status code prefix
        String customMessage = httpClientErrorException.getResponseBodyAsString();
        if (customMessage.isEmpty()) {
            // If no response body, try to extract custom message from the exception message
            String fullMessage = httpClientErrorException.getMessage();
            if (fullMessage != null && fullMessage.contains(" ")) {
                // Remove the status code prefix (e.g., "401 " from "401 No Bearer token passed")
                customMessage = fullMessage.substring(fullMessage.indexOf(" ") + 1);
            } else {
                customMessage = fullMessage;
            }
        }
        
        final ErrorResponse error = ErrorResponse.builder()
                .error(String.valueOf(httpClientErrorException.getStatusCode().value()))
                .message(customMessage)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .traceId(Objects.requireNonNull(tracer.currentSpan()).context().traceId())
                .build();

        return ResponseEntity
                .status(httpClientErrorException.getStatusCode())
                .body(error);
    }
}