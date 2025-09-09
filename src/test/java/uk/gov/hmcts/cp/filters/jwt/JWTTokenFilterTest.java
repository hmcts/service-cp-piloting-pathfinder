package uk.gov.hmcts.cp.filters.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.filters.jwt.JWTTokenFilter.JWT_TOKEN_HEADER;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class JWTTokenFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private JWTTokenFilter jwtTokenFilter;

    @Test
    void shouldErrorIfNoJwtInHeader() {
        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(() -> jwtTokenFilter.doFilterInternal(request, response, filterChain))
                .withMessageContaining("No jwt token passed");
    }

    @Test
    void shouldPassThroughIfPassedJwt() throws ServletException, IOException, InvalidJWTException {
        final String jwt = "dummy-token";
        when(request.getHeader(JWT_TOKEN_HEADER)).thenReturn(jwt);
        when(jwtTokenProvider.validateToken(jwt)).thenReturn(true);
        jwtTokenFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

}