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
abstract public class WireMockTestSetup {
    protected static String wiremockBaseUrl;

    protected static RSAKey rsaKey;

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
}
