package uk.gov.hmcts.cp.filters.auth;

import java.io.IOException;
import java.util.stream.Stream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Component
@ConditionalOnProperty(name = "auth.provider", havingValue = StubOAuthFilter.STUB_AUTH_PROVIDER)
@Slf4j
public class StubOAuthFilter extends OncePerRequestFilter {
    public final static String STUB_AUTH_PROVIDER = "oauth-stub";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final JWTService jwtService;
    private final PathMatcher pathMatcher;
    private final ObjectProvider<AuthDetails> jwtProvider;

    public StubOAuthFilter(final JWTService jwtService,
                           final PathMatcher pathMatcher,
                           final ObjectProvider<AuthDetails> jwtProvider
    ) {
        this.jwtService = jwtService;
        this.pathMatcher = pathMatcher;
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {

        log.info("Stub OAuth Filter invoked");
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("StubOAuthFilter expected header {} with Bearer token not passed", AUTHORIZATION_HEADER);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No Bearer token passed");
        }

        final String jwt = authHeader.substring(7); // Remove "Bearer " prefix
        try {
            final AuthDetails extractedToken = jwtService.extract(jwt);

            AuthDetails requestScopedToken = jwtProvider.getObject(); // current request instance
            requestScopedToken.setUserName(extractedToken.getUserName());
            requestScopedToken.setScope(extractedToken.getScope());
            log.info("Authenticated user {} with scope {}", extractedToken.getUserName(), extractedToken.getScope());
        } catch (InvalidJWTException e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {

        final String safeURI = Encode.forJava(request.getRequestURI());
        log.debug("OAuth Filter, checking should not filter {}", safeURI);
        return Stream.of("/health")
                .anyMatch(p -> pathMatcher.match(p, safeURI));
    }
}
