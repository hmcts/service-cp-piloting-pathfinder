package uk.gov.hmcts.cp.filters.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.nimbusds.jose.util.JSONObjectUtils.*;
import static java.security.KeyPairGenerator.*;

import java.security.interfaces.RSAPublicKey;
import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@WireMockTest
public class WireMockTestSetup {
    protected static String wiremockBaseUrl;

    protected static RSAKey rsaKey;

    @BeforeAll
    static void beforeAll(final WireMockRuntimeInfo wiremock) throws Exception {
        wiremockBaseUrl = wiremock.getHttpBaseUrl();
        final String kid = "test-key";
        final java.security.KeyPairGenerator kpg = getInstance("RSA");
        kpg.initialize(2048);
        final java.security.KeyPair keyPair = kpg.generateKeyPair();
        rsaKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey(keyPair.getPrivate())
                .keyID(kid)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    @BeforeEach
    void stubJwks(final WireMockRuntimeInfo wireMockRuntimeInfo) {
        final String jwks = toJSONString(new JWKSet(List.of(rsaKey.toPublicJWK())).toJSONObject());

        wireMockRuntimeInfo.getWireMock().resetMappings(); // optional: start clean
        wireMockRuntimeInfo.getWireMock().register(
                WireMock.get(urlPathEqualTo("/.well-known/jwks.json"))
                        .withName("jwks-endpoint")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(jwks)))
        ;
    }


    @DynamicPropertySource
    protected static void props(final DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> wiremockBaseUrl + "/.well-known/jwks.json");
    }
}
