package uk.gov.hmcts.cp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.cp.filter.AuthDetails;

@Configuration
public class AuthDetailsConfig {

    @Bean
    @RequestScope
    AuthDetails authDetails(){
        return AuthDetails.builder().build();
    }
}
