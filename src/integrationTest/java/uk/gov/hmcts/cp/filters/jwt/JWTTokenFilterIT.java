package uk.gov.hmcts.cp.filters.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cp.filters.jwt.JWTTokenFilter.JWT_TOKEN_HEADER;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest()
@AutoConfigureMockMvc
class JWTTokenFilterIT {

    @Resource
    MockMvc mockMvc;

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Test
    void shouldPassWhenTokenIsValid() throws Exception {
        String jwtToken = createJWTToken();
        mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/")
                                .header(JWT_TOKEN_HEADER, jwtToken)
                ).andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenTokenIsMissing() {
        assertThatExceptionOfType(HttpClientErrorException.class)
                .isThrownBy(() -> mockMvc
                        .perform(
                                MockMvcRequestBuilders.get("/")
                        ))
                .withMessageContaining("No jwt token passed");
    }

    private String createJWTToken() {
        Date now = new Date();
        System.out.println("Creating JWT token with key: " + secretKey);
        long oneHour = Duration.ofHours(1L).get(ChronoUnit.SECONDS) * 1000;
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .subject("testUser")
                .issuedAt(now)
                .claim("scope", "read write")
                .expiration(new Date(now.getTime() + oneHour))
                .signWith(secretKey)
                .compact();
    }
}