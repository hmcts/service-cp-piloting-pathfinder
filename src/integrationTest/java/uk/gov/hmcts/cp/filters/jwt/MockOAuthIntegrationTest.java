package uk.gov.hmcts.cp.filters.jwt;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
        "auth.provider=oauth-mock"
})
@AutoConfigureMockMvc
class MockOAuthIntegrationTest {

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
    void shouldAcceptValidBearerToken() throws Exception {
        String token = createHs256Token("alice", "read write");
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        content().string(org.hamcrest.Matchers.containsString("Welcome"))
                );
    }

    @Test
    void shouldRejectMissingToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isUnauthorized());
    }

    private String createHs256Token(String subject, String scope) {
        SecretKey key = new SecretKeySpec(SECRET_BYTES, "HmacSHA256");
        Date now = new Date();
        Date exp = new Date(now.getTime() + Duration.ofMinutes(10).toMillis());
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(exp)
                .claim("scope", scope)
                .signWith(key)
                .compact();
    }
}


