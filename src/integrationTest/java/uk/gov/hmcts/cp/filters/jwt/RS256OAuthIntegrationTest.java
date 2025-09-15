package uk.gov.hmcts.cp.filters.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.hmcts.cp.services.SecureService;

import java.time.Instant;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {"auth.provider=oauth-RS256", "auth.enable.method.security=true"})
@AutoConfigureMockMvc
class RS256OAuthIntegrationTest extends WireMockTestSetup {

    @Resource
    MockMvc mockMvc;

    @Resource
    SecureService securedService;


    @Test
    void shouldAcceptValidBearerToken(WireMockRuntimeInfo wm) throws Exception {
        String token = createRs256Token("alice", "read");
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "admin_user", roles={"ADMIN"})
    @Test
    void shouldAccessSecureMethodAsAdminUser() {
        assertThat(securedService.accessUserData()).isEqualTo("Accessible to only user or admin");
    }

    @WithMockUser(username = "user")
    @Test
    void shouldAccessSecureMethodAsUser() {
        assertThat(securedService.accessUserData()).isEqualTo("Accessible to only user or admin");
    }

    @Test
    public void shouldNotAllowAccessToSecuredMethodForUnauthenticatedUser() {
        assertThatExceptionOfType(AuthenticationCredentialsNotFoundException.class)
                .isThrownBy(() -> securedService.accessUserData());
    }

    private String createRs256Token(String subject, String scope) throws Exception {
        var claims = new com.nimbusds.jwt.JWTClaimsSet.Builder()
                .subject(subject)
                .issueTime(java.util.Date.from(Instant.now()))
                .issuer(wiremockBaseUrl)
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(600)))
                .claim("scope", scope)
                .build();

        SignedJWT signed = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        signed.sign(new RSASSASigner(rsaKey.toPrivateKey()));
        return signed.serialize();
    }
}


