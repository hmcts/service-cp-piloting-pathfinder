package uk.gov.hmcts.cp.filters.auth;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class JWTServiceTest {

    private final String secretKey = "it-must-be-a-string-secret-at-least-256-bits-long";
    private final JWTService tokenService = new JWTService(secretKey);

    @DisplayName("A valid JWT must be detected as valid")
    @Test
    void should_validate_jwt(final SoftAssertions softly) throws Exception {
        final String validJWT = tokenService.createToken();
        final AuthDetails authDetails = tokenService.extract(validJWT);

        softly.assertThat(authDetails.getUserName()).isNotNull();
        softly.assertThat(authDetails.getScope()).isNotNull();

        // Optional: one-liner alternative
        // softly.assertThat(authDetails)
        //       .extracting(AuthDetails::getUserName, AuthDetails::getScope)
        //       .doesNotContainNull();
    }

    @DisplayName("An incorrectly signed JWT must be detected as invalid")
    @Test
    void should_invalidate_incorrect_signature_jwt() {
        final String invalidSignatureJWT =
                new JWTService("i_am_some_different_signing_key_than_the_setup").createToken();

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.extract(invalidSignatureJWT))
                .withMessageContaining("Invalid signature");
    }

    @DisplayName("A JWT in unsupported format must be detected as invalid")
    @Test
    void should_invalidate_unsupported_format_jwt() {
        final String unsupportedFormatJWT = Jwts.builder()
                .subject("testUser")
                .issuedAt(Date.from(Instant.now()))
                .claim("scope", "read write")
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .compact();

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.extract(unsupportedFormatJWT))
                .withMessageContaining("Unsupported token");
    }

    @DisplayName("A malformed JWT must be detected as invalid")
    @Test
    void should_invalidate_malformed_jwt() {
        final String extraDot4SegmentInsteadOf3JWT =
                "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiam9obiJ9..invalidsignature.";

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.extract(extraDot4SegmentInsteadOf3JWT))
                .withMessageContaining("Malformed token");
    }

    @DisplayName("An empty JWT must be detected as invalid")
    @Test
    void should_invalidate_empty_jwt() {
        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> tokenService.extract(""))
                .withMessageContaining("JWT token is empty");
    }
}