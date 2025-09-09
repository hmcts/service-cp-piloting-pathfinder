package uk.gov.hmcts.cp.filters.jwt;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private final String secretKey = "it-must-be-a-string-secret-at-least-256-bits-long";
    private final JwtTokenProvider provider = new JwtTokenProvider(secretKey);
    private final SecretKey signingKey = provider.getSecretSigningKey();

    private final Date futureDate = Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
    private final Date expiredDate = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));

    @DisplayName("A valid JWT must be detected as valid")
    @Test
    public void shouldValidateJWT() throws Exception {
        final String validJWT = createToken(signingKey, futureDate);
        boolean isValid = provider.validateToken(validJWT);

        assertTrue(isValid);
    }

    @DisplayName("An expired JWT must be detected as invalid")
    @Test
    public void shouldInvalidateExpiredJWT() {
        final String expiredJWT = createToken(signingKey, expiredDate);

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> provider.validateToken(expiredJWT))
                .withMessageContaining("Expired tokens");
    }

    @DisplayName("An incorrectly signed JWT must be detected as invalid")
    @Test
    public void shouldInvalidateIncorrectSignatureJWT() {
        final SecretKey someDifferentSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode("i_am_some_different_signing_key_than_the_setup"));
        final String invalidSignatureJWT = createToken(someDifferentSigningKey);

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> provider.validateToken(invalidSignatureJWT))
                .withMessageContaining("Invalid signature");
    }

    @DisplayName("A JWT in unsupported format must be detected as invalid")
    @Test
    public void shouldInvalidateUnsupportedFormatJWT() {
        final String unsupportedFormatJWT = Jwts.builder()
                .subject("testUser")
                .issuedAt(new Date())
                .claim("scope", "read write")
                .expiration(futureDate)
                .compact();

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> provider.validateToken(unsupportedFormatJWT))
                .withMessageContaining("Unsupported token");
    }

    @DisplayName("A malformed JWT in must be detected as invalid")
    @Test
    public void shouldInvalidateMalformedJWT() {
        final String extraDot4SegmentInsteadOf3JWT = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyIjoiam9obiJ9..invalidsignature.";

        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> provider.validateToken(extraDot4SegmentInsteadOf3JWT))
                .withMessageContaining("Malformed token");
    }

    @DisplayName("A malformed JWT in must be detected as invalid")
    @Test
    public void shouldInvalidateEmptyJWT() {
        assertThatExceptionOfType(InvalidJWTException.class)
                .isThrownBy(() -> provider.validateToken(""))
                .withMessageContaining("JWT token is empty");
    }

    private String createToken(SecretKey secretKey) {
        return createToken(secretKey, futureDate);
    }

    private String createToken(SecretKey secretKey, Date expiryDate) {
        Date now = new Date();
        return Jwts.builder()
                .subject("testUser")
                .issuedAt(now)
                .claim("scope", "read write")
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }


}