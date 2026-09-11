package dev.omercanbasboga.ssogateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * OAuth2/OIDC login for the gateway itself (authorization-code flow), plus CORS
 * for the SPA frontend and RP-initiated logout against the identity provider.
 * Everything environment-specific (redirect targets, allowed origins) comes from
 * config rather than being hardcoded, so the same image runs in any environment.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final String postLogoutRedirectUri;
    private final List<String> allowedOrigins;

    public SecurityConfig(
            @Value("${app.base-uri}") String postLogoutRedirectUri,
            @Value("${app.allowed-origins}") List<String> allowedOrigins) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ServerLogoutSuccessHandler oidcLogoutSuccessHandler) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/",
                                "/login",
                                "/logout",
                                "/index.html",
                                "/static/**",
                                "/assets/**",
                                "/*.js", "/*.css", "/*.png", "/*.jpg",
                                "/api/oauth2/userinfo",      // read by the frontend after login
                                "/oauth2/**",                // login flow entry point
                                "/login/oauth2/code/**"      // login flow callback
                        ).permitAll()

                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/oidc/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/sso-gateway"))
                );

        return http.build();
    }

    @Bean
    public ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrations) {
        OidcClientInitiatedServerLogoutSuccessHandler handler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrations);

        handler.setPostLogoutRedirectUri(postLogoutRedirectUri);
        return handler;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
