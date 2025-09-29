# Authentication Providers

This service supports multiple authentication providers that can be switched via configuration. Each provider offers different authentication mechanisms suitable for various deployment scenarios.

## Overview

The service supports three authentication providers:

1. **`jwt`** - Custom JWT filter with header-based authentication (default)
2. **`oauth-stub`** - OAuth2 Resource Server with HS256 (HMAC) tokens
3. **`oauth-RS256`** - OAuth2 Resource Server with RS256 (RSA) tokens via JWKS

## Provider Configuration

### Switching Between Providers

Set the `AUTH_PROVIDER` environment variable or `auth.provider` property:

```bash
# Environment variable
export AUTH_PROVIDER=jwt
export AUTH_PROVIDER=oauth-stub
export AUTH_PROVIDER=oauth-RS256
```

```yaml
# application.yaml
auth:
  provider: jwt  # or oauth-stub, oauth-RS256
```

### Method-Level Security

Enable method-level security for all providers:

```yaml
auth:
  enable:
    method:
      security: true
```

## Provider Details

### 1. JWT Provider (`jwt`)

**Purpose**: Custom JWT filter that validates tokens from the `jwt` header.

**Configuration**:
```yaml
auth:
  provider: jwt
jwt:
  secretKey: ${JWT_SECRET_KEY:it-must-be-a-string-secret-at-least-256-bits-long}
  filter:
    enabled: true
```

**Required Environment Variables**:
- `JWT_SECRET_KEY`: Base64-encoded secret key (minimum 256 bits)

**Usage**:
```bash
# Generate a token using JWTService
curl -H "jwt: <your-jwt-token>" http://localhost:4550/
```

**Integration Test**: `JWTFilterIntegrationTest`

### 2. OAuth Mock Provider (`oauth-stub`)

**Purpose**: OAuth2 Resource Server using HS256 (HMAC) tokens for testing and development.

**Configuration**:
```yaml
auth:
  provider: oauth-stub
```

**Required Environment Variables**:
- `JWT_SECRET_KEY`: Base64-encoded secret key for HMAC signing

**Usage**:
```bash
# Generate HS256 token and use Bearer authentication
curl -H "Authorization: Bearer <your-hs256-token>" http://localhost:4550/
```

**Integration Test**: `MockOAuthIntegrationTest`

### 3. OAuth RS256 Provider (`oauth-RS256`)

**Purpose**: OAuth2 Resource Server using RS256 (RSA) tokens with JWKS endpoint for production.

**Configuration**:
```yaml
auth:
  provider: oauth-RS256
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI}
```

**Required Environment Variables**:
- `OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI`: JWKS endpoint URL

**Usage**:
```bash
# Use RS256 token with Bearer authentication
curl -H "Authorization: Bearer <your-rs256-token>" http://localhost:4550/
```

**Integration Test**: `RS256OAuthIntegrationTest`

## Method-Level Security

When `auth.enable.method.security=true`, you can use Spring Security annotations:

```java
@Service
public class SecureService {
    @PreAuthorize("isAuthenticated()")
    public String accessUserData() {
        return "Accessible to authenticated users only";
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnlyMethod() {
        return "Admin only access";
    }
}
```

## Integration Tests

Each provider has corresponding integration tests:

### JWT Provider Tests
```java
@SpringBootTest(properties = {"filter.enable=true", "auth.provider=jwt"})
class JWTFilterIntegrationTest {
    @Test
    void shouldPassWhenTokenIsValid() throws Exception {
        String jwtToken = jwtService.createToken();
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                .header(JWT_TOKEN_HEADER, jwtToken))
                .andExpect(status().isOk());
    }
}
```

### OAuth Mock Provider Tests
```java
@SpringBootTest(properties = {"auth.provider=oauth-stub"})
class MockOAuthIntegrationTest {
    @Test
    void shouldAcceptValidBearerToken() throws Exception {
        String token = createHs256Token("alice", "read write");
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
```

### OAuth RS256 Provider Tests
```java
@SpringBootTest(properties = {"auth.provider=oauth-RS256", "auth.enable.method.security=true"})
@WireMockTest
class RS256OAuthIntegrationTest {
    @Test
    void shouldAcceptValidBearerToken() throws Exception {
        String token = createRs256Token("alice", "read");
        mockMvc.perform(MockMvcRequestBuilders.get("/")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
    
    @WithMockUser(username = "user")
    @Test
    void shouldAccessSecureMethodAsUser() {
        assertThat(securedService.accessUserData())
            .isEqualTo("Accessible to only user or admin");
    }
}
```

## Security Configuration Classes

Each provider has its own security configuration:

- **`HeaderJwtSecurityConfig`**: Configures JWT filter mode
- **`MockOAuthSecurityConfig`**: Configures OAuth2 with HS256
- **`RS256OAuthSecurityConfig`**: Configures OAuth2 with RS256/JWKS
- **`MethodSecurityConfig`**: Enables method-level security

## Excluded Paths

All providers exclude the `/health` endpoint from authentication requirements.

## Error Handling

### JWT Provider
- Missing `jwt` header: 401 UNAUTHORIZED
- Invalid token: 400 BAD_REQUEST

### OAuth Providers
- Missing `Authorization` header: 401 UNAUTHORIZED
- Invalid token: 401 UNAUTHORIZED
- Expired token: 401 UNAUTHORIZED

## Development vs Production

### Development
- Use `jwt` or `oauth-stub` providers
- Simpler setup with shared secret keys
- Suitable for local development and testing

### Production
- Use `oauth-RS256` provider
- JWKS endpoint for token validation
- Better security with asymmetric keys
- Integration with external OAuth2 providers

## Migration Guide

### From JWT to OAuth Mock
1. Change `auth.provider` from `jwt` to `oauth-stub`
2. Ensure `JWT_SECRET_KEY` is set
3. Update client to use `Authorization: Bearer` header instead of `jwt` header

### From OAuth Mock to OAuth RS256
1. Change `auth.provider` from `oauth-stub` to `oauth-RS256`
2. Set `OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI` environment variable
3. Ensure JWKS endpoint is accessible
4. Update token generation to use RS256 instead of HS256

## Troubleshooting

### Common Issues

1. **JWKS URL not accessible**: Ensure `OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI` points to a valid JWKS endpoint
2. **Token validation fails**: Check that the secret key matches between token generation and validation
3. **Method security not working**: Ensure `auth.enable.method.security=true` is set
4. **Filter not running**: Check `filter.enable=true` for JWT provider

### Debugging

Enable debug logging:
```yaml
logging:
  level:
    uk.gov.hmcts.cp.filters.jwt: DEBUG
    org.springframework.security: DEBUG
```

## Related Documentation

- [JWTFilter.md](./JWTFilter.md) - Detailed JWT filter documentation
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [JWKS Specification](https://tools.ietf.org/html/rfc7517)

