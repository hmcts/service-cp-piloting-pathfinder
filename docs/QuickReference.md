# Authentication Quick Reference

## Provider Configuration

| Provider | Environment Variable | Required Config |
|----------|-------------------|-----------------|
| `jwt` | `AUTH_PROVIDER=jwt` | `JWT_SECRET_KEY`, `JWT_FILTER_ENABLED=true` |
| `oauth-stub` | `AUTH_PROVIDER=oauth-stub` | `JWT_SECRET_KEY` |
| `oauth-RS256` | `AUTH_PROVIDER=oauth-RS256` | `OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI` |

## Request Headers

| Provider | Header Format | Example |
|----------|---------------|---------|
| `jwt` | `jwt: <token>` | `jwt: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| `oauth-stub` | `Authorization: Bearer <token>` | `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` |
| `oauth-RS256` | `Authorization: Bearer <token>` | `Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...` |

## Test Configuration

### JWT Provider Test
```java
@SpringBootTest(properties = {
    "auth.provider=jwt",
    "filter.enable=true"
})
class JWTFilterIntegrationTest {
    // Tests use 'jwt' header
}
```

### OAuth Mock Test
```java
@SpringBootTest(properties = {
    "auth.provider=oauth-stub"
})
class MockOAuthIntegrationTest {
    // Tests use 'Authorization: Bearer' header
}
```

### OAuth RS256 Test
```java
@SpringBootTest(properties = {
    "auth.provider=oauth-RS256",
    "auth.enable.method.security=true"
})
@WireMockTest
class RS256OAuthIntegrationTest {
    // Tests use 'Authorization: Bearer' header with WireMock JWKS
}
```

## Method-Level Security

Enable with `AUTH_ENABLE_METHOD_SECURITY=true`:

```java
@Service
public class SecureService {
    @PreAuthorize("isAuthenticated()")
    public String accessUserData() {
        return "Authenticated access";
    }
    
    @WithMockUser(username = "user")
    @Test
    void testAuthenticatedAccess() {
        // Test authenticated method access
    }
}
```

## Common Commands

```bash
# Run all integration tests
./gradlew integration

# Run specific provider tests
./gradlew integration --tests "*JWTFilterIntegrationTest*"
./gradlew integration --tests "*MockOAuthIntegrationTest*"
./gradlew integration --tests "*RS256OAuthIntegrationTest*"

# Run with specific provider
AUTH_PROVIDER=jwt ./gradlew bootRun
AUTH_PROVIDER=oauth-stub ./gradlew bootRun
AUTH_PROVIDER=oauth-RS256 ./gradlew bootRun
```

## Environment Variables Summary

```bash
# Provider selection
AUTH_PROVIDER=jwt|oauth-stub|oauth-RS256

# JWT provider specific
JWT_SECRET_KEY="base64-secret-key"
JWT_FILTER_ENABLED=true

# OAuth RS256 specific
OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI="https://auth-server/.well-known/jwks.json"

# Method-level security
AUTH_ENABLE_METHOD_SECURITY=true
```

