package uk.gov.hmcts.cp.controllers;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import uk.gov.hmcts.cp.openapi.model.ErrorResponse;
import io.micrometer.tracing.TraceContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(SoftAssertionsExtension.class)
class GlobalExceptionHandlerTest {

    @Test
    void handle_response_status_exception_should_return_error_response_with_correct_fields(final SoftAssertions softly) {
        // Arrange
        final Tracer tracer = mock(Tracer.class);
        final Span span = mock(Span.class);
        final TraceContext context = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        final String testTraceId = "test-trace-id";
        when(context.traceId()).thenReturn(testTraceId);

        final GlobalExceptionHandler handler = new GlobalExceptionHandler(tracer);

        final String reason = "Test error";
        final ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, reason);

        // Act
        final ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        final ErrorResponse error = response.getBody();

        softly.assertThat(error.getError()).isEqualTo("404");
        softly.assertThat(error.getMessage()).isEqualTo(reason);
        softly.assertThat(error.getTimestamp()).isNotNull();
        softly.assertThat(error.getTraceId()).isEqualTo(testTraceId);

        softly.assertAll(); // <- reports all failures together
    }

    @Test
    void handle_http_client_error_exception_should_return_error_response_with_correct_fields(final SoftAssertions softly) {
        // Arrange
        final Tracer tracer = mock(Tracer.class);
        final Span span = mock(Span.class);
        final TraceContext context = mock(TraceContext.class);

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(context);
        when(context.traceId()).thenReturn("test-trace-id");

        final GlobalExceptionHandler handler = new GlobalExceptionHandler(tracer);

        final String message = "No Bearer token passed";
        final HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, message);

        // Act
        final ResponseEntity<ErrorResponse> response = handler.handleHttpClientErrorException(exception);

        // Assert
        softly.assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        final ErrorResponse error = response.getBody();
        softly.assertThat(error).isNotNull();

        softly.assertThat(error.getError()).isEqualTo("401");
        softly.assertThat(error.getMessage()).isEqualTo(message);
        softly.assertThat(error.getTimestamp()).isNotNull();
        softly.assertThat(error.getTraceId()).isEqualTo("test-trace-id");
    }
}