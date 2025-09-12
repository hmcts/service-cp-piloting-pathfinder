package uk.gov.hmcts.cp.filters.jwt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(properties = {"auth.provider=oauth-RS256"})
@AutoConfigureMockMvc
@WireMockTest
class RS256OAuthIntegrationTest {
    private static String wiremockBaseUrl;

    @Resource
    MockMvc mockMvc;

    private static RSAKey rsaKey;

    @BeforeAll
    static void beforeAll(WireMockRuntimeInfo wiremock) throws Exception {
        wiremockBaseUrl = wiremock.getHttpBaseUrl();
        String kid = "test-key";
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        java.security.KeyPair kp = kpg.generateKeyPair();
        rsaKey = new RSAKey.Builder((java.security.interfaces.RSAPublicKey) kp.getPublic())
                .privateKey(kp.getPrivate())
                .keyID(kid)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    @BeforeEach
    void stubJwks(WireMockRuntimeInfo wm) {
        String jwks = com.nimbusds.jose.util.JSONObjectUtils.toJSONString(new JWKSet(List.of(rsaKey.toPublicJWK())).toJSONObject());

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
    void shouldAcceptValidBearerToken(WireMockRuntimeInfo wm) throws Exception {
        String token = createRs256Token("alice", "read");
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
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


