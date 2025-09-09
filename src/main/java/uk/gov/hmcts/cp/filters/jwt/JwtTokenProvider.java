package uk.gov.hmcts.cp.filters.jwt;

import javax.crypto.SecretKey;

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
public class JwtTokenProvider {

    private final String secretKey;

    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey) {
        log.debug("Secret key: {}", secretKey);
        this.secretKey = secretKey;
    }

    public boolean validateToken(String token) throws InvalidJWTException {
        try {
            Jwts.parser()
                    .verifyWith((getSecretSigningKey())).build().parse(token);
        } catch (SignatureException ex) {
            log.error("Invalid signature/claims", ex);
            throw new InvalidJWTException("Invalid signature:" + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired tokens", ex);
            throw new InvalidJWTException("Expired tokens:" + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported token", ex);
            throw new InvalidJWTException("Unsupported token:" + ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Malformed token", ex);
            throw new InvalidJWTException("Malformed token:" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT token is empty", ex);
            throw new InvalidJWTException("JWT token is empty:" + ex.getMessage());
        } catch (Exception ex) {
            log.error("Could not verify JWT token integrity", ex);
            throw new InvalidJWTException("Could not validate JWT:" + ex.getMessage());
        }
        return true;
    }

    SecretKey getSecretSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
