package uk.gov.hmcts.cp.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "auth.provider", havingValue = "jwt", matchIfMissing = true)
public class HeaderJwtSecurityConfig {

    @Bean
    public SecurityFilterChain jwtModeSecurityChain(final HttpSecurity http) throws Exception {
        // In header-JWT mode, authorization is handled by JWTFilter; allow requests through here
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}


