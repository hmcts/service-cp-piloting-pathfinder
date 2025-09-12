package uk.gov.hmcts.cp.filters.auth;

import uk.gov.hmcts.cp.config.security.RS256OAuthSecurityConfig;

import java.io.IOException;
import java.util.stream.Stream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Component
@ConditionalOnProperty(name = "auth.provider", havingValue = RS256JWTFilter.OAUTH_RESOURCE_SERVER_AUTH_PROVIDER)
@Slf4j
public class RS256JWTFilter extends OncePerRequestFilter {
    public final static String OAUTH_RESOURCE_SERVER_AUTH_PROVIDER = "oauth-RS256";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final PathMatcher pathMatcher;
    private final ObjectProvider<AuthDetails> jwtProvider;
    private final RS256OAuthSecurityConfig rs256OAuthSecurityConfig;

    public RS256JWTFilter(final PathMatcher pathMatcher,
                          final ObjectProvider<AuthDetails> jwtProvider,
                          @Autowired final RS256OAuthSecurityConfig rs256OAuthSecurityConfig
    ) {
        this.pathMatcher = pathMatcher;
        this.jwtProvider = jwtProvider;
        this.rs256OAuthSecurityConfig = rs256OAuthSecurityConfig;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {

        log.info("RS256JWTFilter Filter invoked");
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("RS256JWTFilter expected header {} with Bearer token not passed", AUTHORIZATION_HEADER);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No Bearer token passed");
        }

        final String jwt = authHeader.substring(7); // Remove "Bearer " prefix
        final String safeJWT = Encode.forJava(request.getRequestURI());
        log.info("JWT token {}", safeJWT);
        final Jwt decodedJWT = rs256OAuthSecurityConfig.jwtDecoder().decode(jwt);
        log.info("decodedJWT claims {}", decodedJWT.getClaims());

        AuthDetails requestScopedToken = jwtProvider.getObject(); // current request instance
        requestScopedToken.setUserName(decodedJWT.getSubject());
        requestScopedToken.setScope(decodedJWT.getClaimAsString("scope"));
        log.info("Authenticated user {} with scope {}", requestScopedToken.getUserName(), requestScopedToken.getScope());


        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return Stream.of("/health")
                .anyMatch(p -> pathMatcher.match(p, request.getRequestURI()));
    }
}
