package uk.gov.hmcts.cp.filters.jwt;

import java.io.IOException;
import java.util.stream.Stream;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
@RequiredArgsConstructor
public class JWTTokenFilter extends OncePerRequestFilter {
    public final static String JWT_TOKEN_HEADER = "jwt";

    private final JwtTokenProvider jwtTokenProvider;
    private final PathMatcher pathMatcher;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {

        final String jwt = request.getHeader(JWT_TOKEN_HEADER);

        if (jwt == null) {
            log.error("JWTTokenFilter expected header {} not passed", JWT_TOKEN_HEADER);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No jwt token passed");
        }

        try {
            jwtTokenProvider.validateToken(jwt);
        } catch (InvalidJWTException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return Stream.of("/health")
                .anyMatch(p  -> pathMatcher.match(p, request.getRequestURI()));
    }
}
