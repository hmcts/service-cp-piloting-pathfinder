package uk.gov.hmcts.cp.filters.auth;

import static org.assertj.core.api.Assertions.assertThat;

import uk.gov.hmcts.cp.services.SecureService;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"auth.provider = oauth-RS256", "auth.enable.method.security = false"})
@AutoConfigureMockMvc
class DisabledMethodLevelSecurityIntegrationTest extends WireMockTestSetup {

    @Resource
    private SecureService securedService;

    @Test
    void should_access_secure_method_When_method_level_security_is_off() {
        assertThat(securedService.accessUserData()).isEqualTo("Accessible to only authenticated users");
    }


}
