package uk.gov.hmcts.cp.config.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(name = "auth.enable.method.security", havingValue = "true")
public class MethodSecurityConfig {}
