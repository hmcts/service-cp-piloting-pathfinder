package uk.gov.hmcts.cp.filters.auth;

import static java.util.Date.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.hmcts.cp.services.SecureService;

import java.time.Instant;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
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
@WireMockTest
class RS256OAuthIntegrationTest extends WireMockTestSetup {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private SecureService securedService;

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    void should_accept_valid_bearer_token() throws Exception {
        final String token = createRs256Token();
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @WithMockUser(username = "admin_user", roles={"ADMIN"})
    @Test
    void should_access_secure_method_as_admin_user() {
        assertThat(securedService.accessUserData()).isEqualTo("Accessible to only authenticated users");
    }

    @WithMockUser(username = "user")
    @Test
    void should_access_secure_method_as_user() {
        assertThat(securedService.accessUserData()).isEqualTo("Accessible to only authenticated users");
    }

    @WithMockUser(username = "user", roles={"GUEST"})
    @Test
    void should_not_access_secure_method_as_guest() {
        assertThat(securedService.accessUserData()).isEqualTo("Accessible to only authenticated users");
    }

    @Test
    void should_not_allow_access_to_secured_method_for_unauthenticated_user() {
        assertThatExceptionOfType(AuthenticationCredentialsNotFoundException.class)
                .isThrownBy(securedService::accessUserData);
    }

    private String createRs256Token() throws Exception {
        final JWTClaimsSet claims = new Builder()
                .subject("alice")
                .issueTime(from(Instant.now()))
                .issuer(wiremockBaseUrl)
                .expirationTime(from(Instant.now().plusSeconds(600)))
                .claim("scope", "read")
                .build();

        final SignedJWT signed = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        signed.sign(new RSASSASigner(rsaKey.toPrivateKey()));
        return signed.serialize();
    }
}


