package uk.gov.hmcts.cp.filters.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.nimbusds.jose.util.JSONObjectUtils.*;
import static java.security.KeyPairGenerator.*;
import static java.util.Date.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.hmcts.cp.services.SecureService;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {"auth.provider=oauth-RS256", "auth.enable.method.security=true"})
@AutoConfigureMockMvc
@WireMockTest
class RS256OAuthIntegrationTest {
    private static String wiremockBaseUrl;

    @Resource
    MockMvc mockMvc;

    @Resource
    SecureService securedService;

    private static RSAKey rsaKey;

    @BeforeAll
    static void beforeAll(WireMockRuntimeInfo wiremock) throws Exception {
        wiremockBaseUrl = wiremock.getHttpBaseUrl();
        String kid = "test-key";
        java.security.KeyPairGenerator kpg = getInstance("RSA");
        kpg.initialize(2048);
        java.security.KeyPair kp = kpg.generateKeyPair();
        rsaKey = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                .privateKey(kp.getPrivate())
                .keyID(kid)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    @BeforeEach
    void stubJwks(WireMockRuntimeInfo wm) {
        String jwks = toJSONString(new JWKSet(List.of(rsaKey.toPublicJWK())).toJSONObject());

        wm.getWireMock().resetMappings(); // optional: start clean
        wm.getWireMock().register(
                WireMock.get(urlPathEqualTo("/.well-known/jwks.json"))
                        .withName("jwks-endpoint")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(jwks)))
        ;
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> wiremockBaseUrl + "/.well-known/jwks.json");
    }


    @Test
    void should_accept_valid_bearer_token() throws Exception {
        String token = createRs256Token();
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
    public void should_not_allow_access_to_secured_method_for_unauthenticated_user() {
        assertThatExceptionOfType(AuthenticationCredentialsNotFoundException.class)
                .isThrownBy(() -> securedService.accessUserData());
    }

    private String createRs256Token() throws Exception {
        var claims = new Builder()
                .subject("alice")
                .issueTime(from(Instant.now()))
                .issuer(wiremockBaseUrl)
                .expirationTime(from(Instant.now().plusSeconds(600)))
                .claim("scope", "read")
                .build();

        SignedJWT signed = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
                claims
        );
        signed.sign(new RSASSASigner(rsaKey.toPrivateKey()));
        return signed.serialize();
    }
}


