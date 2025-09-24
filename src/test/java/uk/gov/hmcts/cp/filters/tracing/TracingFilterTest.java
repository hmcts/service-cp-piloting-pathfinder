package uk.gov.hmcts.cp.filters.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.filters.tracing.TracingFilter.*;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

@ExtendWith(MockitoExtension.class)
class TracingFilterTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private TracingFilter tracingFilter = new TracingFilter("myAppName");

    @Test
    void filter_should_use_incoming_traceId() throws ServletException, IOException {
        when(request.getHeader(TRACE_ID)).thenReturn("incoming-traceId");
        when(request.getHeader(SPAN_ID)).thenReturn("incoming-spanId");

        tracingFilter.doFilterInternal(request, response, filterChain);

        assertThat(MDC.get(APPLICATION_NAME)).isEqualTo("myAppName");
        verify(response).setHeader(TRACE_ID, "incoming-traceId");
        assertThat(MDC.get(TRACE_ID)).isEqualTo("incoming-traceId");
        verify(response).setHeader(SPAN_ID, "incoming-spanId");
        assertThat(MDC.get(SPAN_ID)).isEqualTo("incoming-spanId");
    }
}