# Service Authentication Documentation

This service supports multiple authentication providers that can be configured for different deployment scenarios.

## Quick Start

### 1. Choose Your Authentication Provider

Set the `AUTH_PROVIDER` environment variable:

```bash
# For custom JWT filter (default)
export AUTH_PROVIDER=jwt

# For OAuth2 with HMAC tokens (testing)
export AUTH_PROVIDER=oauth-stub

# For OAuth2 with RSA tokens (production)
export AUTH_PROVIDER=oauth-RS256
```

### 2. Configure Required Environment Variables

#### JWT Provider
```bash
export JWT_SECRET_KEY="your-base64-secret-key"
export JWT_FILTER_ENABLED=true
```

#### OAuth Mock Provider
```bash
export JWT_SECRET_KEY="your-base64-secret-key"
```

#### OAuth RS256 Provider
```bash
export OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI="https://your-auth-server/.well-known/jwks.json"
```

### 3. Enable Method-Level Security (Optional)

```bash
export AUTH_ENABLE_METHOD_SECURITY=true
```

## Documentation

- **[AuthenticationProviders.md](./AuthenticationProviders.md)** - Complete guide to all authentication providers, configuration, and usage examples
- **[Examples.md](./Examples.md)** - Practical examples for each authentication provider
- **[QuickReference.md](./QuickReference.md)** - Quick reference guide for developers
- **[JWTFilter.md](./JWTFilter.md)** - Detailed documentation for the JWT filter provider

## Authentication Providers Overview

| Provider | Use Case | Token Type | Header | Security |
|----------|----------|------------|--------|----------|
| `jwt` | Custom integration | HS256 | `jwt` | Shared secret |
| `oauth-stub` | Testing/Development | HS256 | `Authorization: Bearer` | Shared secret |
| `oauth-RS256` | Production | RS256 | `Authorization: Bearer` | Asymmetric keys |

## Integration Tests

Each provider has comprehensive integration tests:

- **JWT Provider**: `JWTFilterIntegrationTest`
- **OAuth Mock**: `MockOAuthIntegrationTest`  
- **OAuth RS256**: `RS256OAuthIntegrationTest`

Run tests for a specific provider:
```bash
./gradlew integration --tests "*JWTFilterIntegrationTest*"
./gradlew integration --tests "*MockOAuthIntegrationTest*"
./gradlew integration --tests "*RS256OAuthIntegrationTest*"
```

## Method-Level Security

When enabled, use Spring Security annotations:

```java
@Service
public class SecureService {
    @PreAuthorize("isAuthenticated()")
    public String accessUserData() {
        return "Accessible to authenticated users only";
    }
}
```

## Migration Between Providers

### From JWT to OAuth Mock
1. Change `AUTH_PROVIDER` from `jwt` to `oauth-stub`
2. Update client to use `Authorization: Bearer` header
3. Ensure `JWT_SECRET_KEY` is set

### From OAuth Mock to OAuth RS256
1. Change `AUTH_PROVIDER` from `oauth-stub` to `oauth-RS256`
2. Set `OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI`
3. Update token generation to use RS256

## Troubleshooting

### Common Issues
- **JWKS URL not accessible**: Check `OAUTH2_RESOURCE_SERVER_JWT_JWK_SET_URI`
- **Token validation fails**: Verify secret key matches
- **Method security not working**: Ensure `AUTH_ENABLE_METHOD_SECURITY=true`

### Debug Logging
```yaml
logging:
  level:
    uk.gov.hmcts.cp.filters.jwt: INFO
    org.springframework.security: INFO
```

## Security Considerations

- **Development**: Use `jwt` or `oauth-stub` with shared secrets
- **Production**: Use `oauth-RS256` with JWKS endpoints
- **Secrets**: Never commit secret keys to version control
- **HTTPS**: Always use HTTPS in production environments
