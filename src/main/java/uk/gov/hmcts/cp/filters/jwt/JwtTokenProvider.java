package uk.gov.hmcts.cp.filters.jwt;

import java.security.Key;

import javax.crypto.SecretKey;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTokenProvider {
    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    //TODO this should come from config
    private final String secretKey;

    public JwtTokenProvider(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean validateToken(String token) throws InvalidJWTException {
        try {
            Jwts.parser()
                    .verifyWith(((SecretKey) getSigningKey())).build().parse(token);
        } catch (SignatureException ex) {
            logger.error("Invalid signature/claims", ex);
            throw new InvalidJWTException("Invalid signature:" + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired tokens", ex);
            throw new InvalidJWTException("Expired tokens:" + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported token", ex);
            throw new InvalidJWTException("Unsupported token:" + ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Malformed token", ex);
            throw new InvalidJWTException("Malformed token:" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT token is empty", ex);
            throw new InvalidJWTException("JWT token is empty:" + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Could not verify JWT token integrity", ex);
            throw new InvalidJWTException("Could not validate JWT:" + ex.getMessage());
        }
        return true;
    }

    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public SecretKey getSecretSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
