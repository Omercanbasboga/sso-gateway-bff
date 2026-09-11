package dev.omercanbasboga.ssogateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Local logout endpoint used by the frontend: clears the gateway-side session
 * and redirects back to the app's own landing page (the OIDC provider logout
 * is handled separately by {@link dev.omercanbasboga.ssogateway.security.SecurityConfig}).
 */
@RestController
public class LogoutController {

    private final String appBaseUri;

    public LogoutController(@Value("${app.base-uri}") String appBaseUri) {
        this.appBaseUri = appBaseUri;
    }

    @GetMapping("/logout-app")
    public Mono<Void> logout(@AuthenticationPrincipal OidcUser user, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(URI.create(appBaseUri));
        return response.setComplete();
    }

}
