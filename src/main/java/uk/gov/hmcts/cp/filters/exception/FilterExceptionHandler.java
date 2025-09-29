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
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException {

        final HttpServletResponse httpResponse = (HttpServletResponse) response;

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


    private void handleHttpClientErrorException(final HttpClientErrorException exception, final HttpServletResponse response)
            throws IOException {
        final int statusCode = exception.getStatusCode().value();
        final String message = extractMessage(exception);

        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final String jsonResponse = String.format(
            "{\"error\":\"%d\",\"message\":\"%s\"}",
            statusCode,
            message
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private void handleGenericException(final Exception exception, final HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        final String jsonResponse = String.format(
            "{\"error\":\"500\",\"message\":\"Internal server error: %s\"}",
            exception.getMessage()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private String extractMessage(final HttpClientErrorException exception) {
        String message = "Unknown error";

        final String responseBody = exception.getResponseBodyAsString();
        if (responseBody.isEmpty()) {
            final String fullMessage = exception.getMessage();
            if (fullMessage != null) {
                final int spaceIdx = fullMessage.indexOf(' ');
                if (spaceIdx >= 0 && spaceIdx < fullMessage.length() - 1) {
                    // Remove the status code prefix (e.g., "401 " from "401 No Bearer token passed")
                    message = fullMessage.substring(spaceIdx + 1);
                } else {
                    message = fullMessage;
                }
            }
        } else {
            message = responseBody;
        }

        return message;
    }
}
