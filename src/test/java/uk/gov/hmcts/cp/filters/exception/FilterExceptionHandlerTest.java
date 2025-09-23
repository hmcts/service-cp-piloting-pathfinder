package uk.gov.hmcts.cp.filters.exception;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class FilterExceptionHandlerTest {

    private FilterExceptionHandler filterExceptionHandler;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockFilterChain;
    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        filterExceptionHandler = new FilterExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        
        when(mockResponse.getWriter()).thenReturn(printWriter);
    }

    @Test
    void should_handle_http_client_error_exception_with_unauthorized() throws IOException, ServletException {
        // Given
        final HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No jwt token passed");
        doThrow(exception).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setStatus(401);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
        
        printWriter.flush();
        final String responseBody = responseWriter.toString();
        assertThatJson(responseBody)
                .isEqualTo("{\"error\":\"401\",\"message\":\"No jwt token passed\"}");
    }

    @Test
    void should_handle_http_client_error_exception_with_bad_request() throws IOException, ServletException {
        // Given
        final HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
        doThrow(exception).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setStatus(400);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
        
        printWriter.flush();
        final String responseBody = responseWriter.toString();
        assertThatJson(responseBody)
                .isEqualTo("{\"error\":\"400\",\"message\":\"Invalid JWT token\"}");
    }

    @Test
    void should_handle_generic_exception() throws IOException, ServletException {
        // Given
        final RuntimeException exception = new RuntimeException("Unexpected error");
        doThrow(exception).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setStatus(500);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
        
        printWriter.flush();
        final String responseBody = responseWriter.toString();
        assertThatJson(responseBody)
                .isEqualTo("{\"error\":\"500\",\"message\":\"Internal server error: Unexpected error\"}");
    }

    @Test
    void should_pass_through_when_no_exception() throws IOException, ServletException {
        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }
}
