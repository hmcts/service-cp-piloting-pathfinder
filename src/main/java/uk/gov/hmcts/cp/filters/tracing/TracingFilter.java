package uk.gov.hmcts.cp.filters.tracing;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class TracingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader("traceId") != null) {
            MDC.put("traceId", request.getHeader("traceId"));
            response.setHeader("traceId", request.getHeader("traceId"));
        }
        if (request.getHeader("spanId") != null) {
            MDC.put("spanId", request.getHeader("spanId"));
            response.setHeader("spanId", request.getHeader("spanId"));
        }
        filterChain.doFilter(request, response);
    }
}
