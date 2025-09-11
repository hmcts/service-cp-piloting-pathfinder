package uk.gov.hmcts.cp.filters.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TracingFilterTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    TracingFilter tracingFilter;

    @Test
    void filter_should_use_incoming_traceId() throws ServletException, IOException {
        when(request.getHeader("traceId")).thenReturn("incoming-traceId");
        when(request.getHeader("spanId")).thenReturn("incoming-spanId");

        tracingFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("traceId", "incoming-traceId");
        assertThat(MDC.get("traceId")).isEqualTo("incoming-traceId");
        verify(response).setHeader("spanId", "incoming-spanId");
        assertThat(MDC.get("spanId")).isEqualTo("incoming-spanId");
    }
}