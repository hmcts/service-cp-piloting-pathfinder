package uk.gov.hmcts.cp.filters.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.filters.jwt.JWTFilter.JWT_TOKEN_HEADER;

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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private JWTService jwtService;
    @Mock
    private ObjectProvider<AuthDetails> jwtProvider;

    @InjectMocks
    private JWTFilter jwtFilter;

    @Test
    void shouldErrorIfNoJwtInHeader() {
        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(() -> jwtFilter.doFilterInternal(request, response, filterChain))
                .withMessageContaining("No jwt token passed");
    }

    @Test
    void shouldPassThroughIfPassedJwt() throws ServletException, IOException, InvalidJWTException {
        final String jwt = "dummy-token";
        when(request.getHeader(JWT_TOKEN_HEADER)).thenReturn(jwt);
        when(jwtService.extract(jwt)).thenReturn(new AuthDetails("testUser", "read write"));
        when(jwtProvider.getObject()).thenReturn(AuthDetails.builder().build());
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

}