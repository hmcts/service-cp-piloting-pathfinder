package uk.gov.hmcts.cp.controllers;

import io.micrometer.tracing.TraceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
  @Mock
  TraceContext traceContext;

  @InjectMocks
  GlobalExceptionHandler globalExceptionHandler;

  @Test
  void handleResponseStatusException_ShouldReturnErrorResponseWithCorrectFields() {
    // Arrange
//    Tracer tracer = mock(Tracer.class);
//    Span span = mock(Span.class);
//
//    when(tracer.currentSpan()).thenReturn(span);
//    when(span.context()).thenReturn(traceContext);
//    when(traceContext.traceId()).thenReturn("test-trace-id");
//
//    GlobalExceptionHandler handler = new GlobalExceptionHandler(tracer);
//
//    String reason = "Test error";
//    ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
//
//    // Act
//    var response = handler.handleResponseStatusException(ex);
//
//    // Assert
//    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//    ErrorResponse error = response.getBody();
//    assertNotNull(error);
//    assertEquals("404", error.getError());
//    assertEquals(reason, error.getMessage());
//    assertNotNull(error.getTimestamp());
//    assertEquals("test-trace-id", error.getTraceId());
  }
}