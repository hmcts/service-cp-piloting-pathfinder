package uk.gov.hmcts.cp.filters.auth;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JWTService {

    private static final Duration ONE_HOUR = Duration.ofHours(1);
    private static final String SCOPE = "scope";
    public static final String USER = "testUser";

    private final String secretKey;

    public JWTService(final @Value("${auth.jwt.secretKey}") String secretKey) {
        this.secretKey = secretKey;
    }

    public AuthDetails extract(final String token) throws InvalidJWTException {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(getSecretSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            final String userName = claims.getSubject();
            final String scope = claims.get(SCOPE).toString();
            return new AuthDetails(userName, scope);
        } catch (SignatureException ex) {
            log.error("Invalid signature/claims {}", ex.toString(), ex);
            throw new InvalidJWTException("Invalid signature:" + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired tokens {}", ex.toString(), ex);
            throw new InvalidJWTException("Expired tokens:" + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported token {}", ex.getMessage());
            throw new InvalidJWTException("Unsupported token:" + ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Malformed token {}", ex.toString(), ex);
            throw new InvalidJWTException("Malformed token:" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT token is empty {}", ex.toString(), ex);
            throw new InvalidJWTException("JWT token is empty:" + ex.getMessage());
        } catch (Exception ex) {
            log.error("Could not verify JWT token integrity {}", ex.toString(), ex);
            throw new InvalidJWTException("Could not validate JWT:" + ex.getMessage());
        }
    }

    public String createToken(final Instant expiry) {
        return Jwts.builder()
                .subject(USER)
                .issuedAt(Date.from(Instant.now()))
                .claim(SCOPE, "read write")
                .expiration(Date.from(expiry))
                .signWith(getSecretSigningKey())
                .compact();
    }

    public String createToken() {
        return createToken(expiryAfterOneHour());
    }

    private SecretKey getSecretSigningKey() {
        final byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Instant expiryAfterOneHour() {
        return Instant.now().plus(1, ChronoUnit.HOURS);
    }
}
