package uk.gov.hmcts.cp.filters.auth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.filters.auth.JWTFilter.AUTHORIZATION_HEADER;

import uk.gov.hmcts.cp.config.security.RS256OAuthSecurityConfig;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.PathMatcher;

@ExtendWith(MockitoExtension.class)
class RS256JWTFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private ObjectProvider<AuthDetails> jwtProvider;
    @Mock
    private PathMatcher pathMatcher;
    @Mock
    private RS256OAuthSecurityConfig config;
    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private Jwt mockJwt;

    @Test
    void should_pass_through_if_passed_jwt_with_oauth_auth_provider() throws ServletException, IOException {
        final String jwt = "Bearer dummy-token";
        final String jwtToken = "dummy-token"; // JWT without "Bearer " prefix

        final RS256JWTFilter rs256FilterEnabled = new RS256JWTFilter(pathMatcher, jwtProvider, config);
        // Mock the JWT with required claims
        when(mockJwt.getSubject()).thenReturn("testUser");
        when(mockJwt.getClaimAsString("scope")).thenReturn("read write");
        when(mockJwt.getClaims()).thenReturn(java.util.Map.of("sub", "testUser", "scope", "read write"));
        
        // Mock the request and dependencies
        when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(jwt);
        when(config.jwtDecoder()).thenReturn(jwtDecoder);
        when(jwtDecoder.decode(jwtToken)).thenReturn(mockJwt);
        when(jwtProvider.getObject()).thenReturn(AuthDetails.builder().build());
        
        rs256FilterEnabled.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }


}