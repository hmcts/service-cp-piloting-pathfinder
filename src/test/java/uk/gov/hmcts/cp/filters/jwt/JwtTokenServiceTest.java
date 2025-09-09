package uk.gov.hmcts.cp.filters.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private final String secretKey = "it-must-be-a-string-secret-at-least-256-bits-long";
    private final JwtTokenService tokenService = new JwtTokenService(secretKey);

    private final Date futureDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
    private final Date expiredDate = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));

    @DisplayName("A valid JWT must be detected as valid")
    @Test
    void shouldValidateJWT() throws Exception {
        final String validJWT = tokenService.createToken();
        boolean isValid = tokenService.validateToken(validJWT);

        assertTrue(isValid);
    }

    @DisplayName("An incorrectly signed JWT must be detected as invalid")
    @Test
    void shouldInvalidateIncorrectSignatureJWT() {
        final String invalidSignatureJWT = new JwtTokenService("i_am_some_different_signing_key_than_the_setup").createToken();

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.validateToken(invalidSignatureJWT))
                .withMessageContaining("Invalid signature");
    }

    @DisplayName("A JWT in unsupported format must be detected as invalid")
    @Test
    void shouldInvalidateUnsupportedFormatJWT() {
        final String unsupportedFormatJWT = Jwts.builder()
                .subject("testUser")
                .issuedAt(new Date())
                .claim("scope", "read write")
                .expiration(futureDate)
                .compact();

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.validateToken(unsupportedFormatJWT))
                .withMessageContaining("Unsupported token");
    }

    @DisplayName("A malformed JWT must be detected as invalid")
    @Test
    void shouldInvalidateMalformedJWT() {
        final String extraDot4SegmentInsteadOf3JWT = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiam9obiJ9..invalidsignature.";

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.validateToken(extraDot4SegmentInsteadOf3JWT))
                .withMessageContaining("Malformed token");
    }

    @DisplayName("An empty JWT must be detected as invalid")
    @Test
    void shouldInvalidateEmptyJWT() {
        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.validateToken(""))
                .withMessageContaining("JWT token is empty");
    }
}