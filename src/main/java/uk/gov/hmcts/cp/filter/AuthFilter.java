package uk.gov.hmcts.cp.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    public final static String JWT_TOKEN_HEADER = "jwt";

    private boolean authFilterEnabled;
    private AuthDetails authDetails;

    public AuthFilter(@Value("${auth.filter.enabled:true}") final boolean authFilterEnabled,
                      AuthDetails authDetails) {
        super();
        this.authFilterEnabled = authFilterEnabled;
        this.authDetails = authDetails;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final String cleanUri = request.getRequestURI()
                .replaceAll("\\n", "")
                .replaceAll("\\r", "");
        log.info("API RequestFilter enabled:{} called for url:{}", authFilterEnabled, cleanUri);
        // We can pick off jwt token in here and validate it and then set the claims into an AuthContext object
        // For now lets just show that we can inject this filter into our parent service and do a simple check
        if (authFilterEnabled && request.getHeader(JWT_TOKEN_HEADER) == null) {
            log.error("API RequestFilter expected header {} not passed", JWT_TOKEN_HEADER);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No jwt token passed");
        }
        // Update the rwquest scoped bean for Auth details
        // We can get the claims from the jwt and add them to the authDetails which is available right
        // throughout the request
        authDetails.setUserName("Demo Authorised User");

        filterChain.doFilter(request, response);
    }
}