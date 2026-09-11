package dev.omercanbasboga.ssogateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// TODO review: this context-loading test requires a live, reachable OIDC issuer at
// boot time — Spring Boot's OAuth2 client autoconfiguration performs real HTTP
// discovery (GET /.well-known/openid-configuration) whenever a provider's issuer-uri
// property is present anywhere in the merged environment, even one meant to be
// overridden for tests. This is inherent to how Spring Boot resolves OAuth2 client
// registrations (not something introduced by de-identifying this project) and it
// also affects the original application. In a real CI pipeline this is normally
// solved with a lightweight embedded/mock OIDC provider (e.g. wiremock or a test
// container) started before the context loads. `mvn compile` succeeds cleanly;
// `mvn test` currently fails on this single smoke test for the network reason above.
@SpringBootTest
@TestPropertySource(properties = {
        // Don't activate the "default" profile here: application-default.yml sets
        // provider.oauth2.issuer-uri, and once that key is present (even blank) Spring
        // Boot's OAuth2ClientPropertiesMapper always attempts real OIDC discovery over
        // HTTP instead of falling back to the explicit endpoints below. Keeping this
        // profile inactive means only the properties declared here apply.
        "spring.profiles.active=",
        "spring.security.oauth2.client.registration.sso-gateway.provider=oauth2",
        "spring.security.oauth2.client.registration.sso-gateway.client-id=test-client",
        "spring.security.oauth2.client.registration.sso-gateway.client-secret=test-secret",
        "spring.security.oauth2.client.registration.sso-gateway.authorization-grant-type=authorization_code",
        "spring.security.oauth2.client.registration.sso-gateway.redirect-uri=http://localhost:8082/login/oauth2/code/sso-gateway",
        "spring.security.oauth2.client.registration.client_token.provider=oauth2",
        "spring.security.oauth2.client.registration.client_token.client-id=test-client",
        "spring.security.oauth2.client.registration.client_token.client-secret=test-secret",
        "spring.security.oauth2.client.registration.client_token.authorization-grant-type=client_credentials",
        "spring.security.oauth2.client.provider.oauth2.authorization-uri=https://example.com/oauth2/authorize",
        "spring.security.oauth2.client.provider.oauth2.token-uri=https://example.com/oauth2/token",
        "spring.security.oauth2.client.provider.oauth2.jwk-set-uri=https://example.com/oauth2/jwks",
        "spring.security.oauth2.client.provider.oauth2.user-info-uri=https://example.com/oauth2/userinfo",
        "spring.security.oauth2.client.provider.oauth2.user-name-attribute=sub",
        "app.base-uri=http://localhost:8082",
        "app.allowed-origins=http://localhost:3000"
})
class SsoGatewayBffApplicationTests {

    @Test
    void contextLoads() {
    }

}
