package com.github.jorgenbjerke.security.workshop;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("swagger-ui.html", "swagger-ui/**", "v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.security.issuer-uri}") String issuer,
            @Value("${app.security.client-id}") String clientId
    ) {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuer);

        jwtDecoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        new JwtClaimValidator<>("aud", verifyClaim(clientId::equals)),
                        new JwtClaimValidator<>("iss", verifyClaim(issuer::equals)),
                        new JwtClaimValidator<>("exp", verifyClaim(Objects::nonNull)),
                        new JwtClaimValidator<>("iat", verifyTimestamp(claim -> Instant.now().isAfter(claim))),
                        new JwtClaimValidator<>("nbf", verifyTimestamp(claim -> Instant.now().isAfter(claim)))
                )
        );

        return jwtDecoder;
    }

    private Predicate<Object> verifyClaim(Predicate<Object> test) {
        return claim -> {
            if (claim == null) {
                return false;
            }
            if (claim instanceof Collection<?>) {
                if (((Collection<?>) claim).size() != 1) {
                    return false;
                } else {
                    claim = ((Collection<?>) claim).toArray()[0];
                }
            }
            return test.test(claim);
        };
    }

    private Predicate<Object> verifyTimestamp(Predicate<Instant> test) {
        return claim -> {
            if (claim instanceof Instant) {
                return test.test((Instant) claim);
            }
            return false;
        };
    }
}
