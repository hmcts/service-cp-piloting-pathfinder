package uk.gov.hmcts.cp.filters.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void shouldHandleHttpClientErrorExceptionWithUnauthorized() throws IOException, ServletException {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No jwt token passed");
        doThrow(exception).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setStatus(401);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
        
        printWriter.flush();
        String responseBody = responseWriter.toString();
        assertEquals("{\"error\":\"401\",\"message\":\"No jwt token passed\"}", responseBody);
    }

    @Test
    void shouldHandleHttpClientErrorExceptionWithBadRequest() throws IOException, ServletException {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
        doThrow(exception).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setStatus(400);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
        
        printWriter.flush();
        String responseBody = responseWriter.toString();
        assertEquals("{\"error\":\"400\",\"message\":\"Invalid JWT token\"}", responseBody);
    }

    @Test
    void shouldHandleGenericException() throws IOException, ServletException {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");
        doThrow(exception).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockResponse).setStatus(500);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
        
        printWriter.flush();
        String responseBody = responseWriter.toString();
        assertEquals("{\"error\":\"500\",\"message\":\"Internal server error: Unexpected error\"}", responseBody);
    }

    @Test
    void shouldPassThroughWhenNoException() throws IOException, ServletException {
        // When
        filterExceptionHandler.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }
}
