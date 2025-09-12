package uk.gov.hmcts.cp.filters.exception;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
@ConditionalOnBooleanProperty(name = "filter.enable")
public class FilterExceptionHandler implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (HttpClientErrorException e) {
            log.error("HttpClientErrorException caught in filter: {}", e.getMessage());
            handleHttpClientErrorException(e, httpResponse);
        } catch (Exception e) {
            log.error("Unexpected exception in filter chain: {}", e.getMessage(), e);
            handleGenericException(e, httpResponse);
        }
    }


    private void handleHttpClientErrorException(HttpClientErrorException e, HttpServletResponse response)
            throws IOException {
        int statusCode = e.getStatusCode().value();
        String message = extractMessage(e);

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
            "{\"error\":\"%d\",\"message\":\"%s\"}",
            statusCode,
            message
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private void handleGenericException(Exception e, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
            "{\"error\":\"500\",\"message\":\"Internal server error: %s\"}",
            e.getMessage()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private String extractMessage(HttpClientErrorException e) {
        String responseBody = e.getResponseBodyAsString();
        if (!responseBody.isEmpty()) {
            return responseBody;
        }

        String fullMessage = e.getMessage();
        if (fullMessage != null && fullMessage.contains(" ")) {
            // Remove the status code prefix (e.g., "401 " from "401 No Bearer token passed")
            return fullMessage.substring(fullMessage.indexOf(" ") + 1);
        }

        return fullMessage != null ? fullMessage : "Unknown error";
    }
}
