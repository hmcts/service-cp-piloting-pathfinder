# JWTFilter

`JWTFilter` enforces the presence of a JWT on incoming requests, validates it, and exposes user details for the lifetime of the request.

> **Note**: This service supports multiple authentication providers. See [AuthenticationProviders.md](./AuthenticationProviders.md) for a complete overview of all available providers and how to switch between them.

## Purpose
- Requires the `jwt` header on requests (except excluded paths)
- Validates and parses the token via `JWTService`
- Stores `userName` and `scope` in a request-scoped `AuthDetails` bean

## Configuration
Defined in `src/main/resources/application.yaml`:

```yaml
auth:
  provider: jwt  # Enable JWT filter mode
jwt:
  secretKey: "it-must-be-a-string-secret-at-least-256-bits-long"
  filter:
    enabled: false
```

- `auth.provider`: Set to `jwt` to enable JWT filter mode (default)
- `auth.jwt.secretKey`: Base64 key suitable for HS256 (≥ 256 bits)
- `filter.enable`: When false, the filter is skipped entirely. When true, it runs for all paths except those excluded.

### Enabling per environment
- Env var: `AUTH_PROVIDER=jwt` and `JWT_FILTER_ENABLED=true`
- Tests: `@SpringBootTest(properties = {"auth.provider=jwt", "filter.enable=true"})`
- Profile override: `application-<profile>.yaml`

## Excluded paths
Currently excluded: `/health`. Extend in `JWTFilter.shouldNotFilter(...)` if needed.

## Error behaviour
- Missing header: 401 UNAUTHORIZED ("No jwt token passed")
- Invalid token: 401 UNAUTHORIZED

## Usage Example

```bash
# Generate a token using JWTService
curl -H "jwt: <your-jwt-token>" http://localhost:4550/
```

## See Also
- [AuthenticationProviders.md](./AuthenticationProviders.md) - Complete guide to all authentication providers
- [Method-Level Security](./AuthenticationProviders.md#method-level-security) - Using `@PreAuthorize` annotations
