package uk.gov.hmcts.cp.filters.auth;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import jakarta.annotation.Resource;

@SpringBootTest(properties = {
        "auth.provider=oauth-stub"
})
@AutoConfigureMockMvc
class StubOAuthIntegrationTest {

    private static final byte[] SECRET_BYTES = "this-is-a-32-byte-minimum-secret-key!!".substring(0, 32)
            .getBytes(StandardCharsets.UTF_8);
    private static final String SECRET_BASE64 = Base64.getEncoder().encodeToString(SECRET_BYTES);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("JWT_SECRET_KEY", () -> SECRET_BASE64);
    }

    @Resource
    MockMvc mockMvc;

    @Test
    void should_accept_valid_bearer_token() throws Exception {
        String token = createHs256Token("read write", Date.from(Instant.now().plusSeconds(600)));
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        content().string(containsString("Welcome to service-hmcts-marketplace-piloting-pathfinder, alice"))
                );
    }

    @Test
    void should_reject_expired_bearer_token() throws Exception {
        String token = createHs256Token("read", Date.from(Instant.now().minusSeconds(600)));
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_reject_missing_token() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isUnauthorized());
    }

    private String createHs256Token(String scope, final Date expiry) {
        SecretKey key = new SecretKeySpec(SECRET_BYTES, "HmacSHA256");
        Date now = new Date();
        return Jwts.builder()
                .subject("alice")
                .issuedAt(now)
                .expiration(expiry)
                .claim("scope", scope)
                .signWith(key)
                .compact();
    }
}


