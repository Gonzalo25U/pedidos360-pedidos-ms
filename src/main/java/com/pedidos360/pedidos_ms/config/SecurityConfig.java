package com.pedidos360.pedidos_ms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@Profile("!local")
public class SecurityConfig {

    @Value("${azure.audience}")
    private String expectedAudience;

    @Value("${azure.client-id}")
    private String clientId;

    @Value("${cognito.issuer}")
    private String cognitoIssuer;

    @Value("${cognito.client-id}")
    private String cognitoClientId;

    @Value("${cors.allowed-origins}")
    private String allowedOrigin;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/pedidos/admin/**").hasRole("Admin")
                .requestMatchers("/api/pedidos/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                .authenticationEntryPoint((request, response, ex) ->
                    response.sendError(401, "No autorizado: token ausente o invalido"))
                .accessDeniedHandler((request, response, ex) ->
                    response.sendError(403, "Prohibido: no cuenta con el rol requerido"))
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder azureDecoder = NimbusJwtDecoder
                .withJwkSetUri("https://login.microsoftonline.com/common/discovery/v2.0/keys")
                .build();
        OAuth2TokenValidator<Jwt> azureValidators = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new MultiTenantIssuerValidator(),
                new AudienceValidator(List.of(expectedAudience, clientId))
        );
        azureDecoder.setJwtValidator(azureValidators);

        NimbusJwtDecoder cognitoDecoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(cognitoIssuer);
        OAuth2TokenValidator<Jwt> cognitoValidators = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(cognitoIssuer),
                new CognitoAudienceValidator(cognitoClientId)
        );
        cognitoDecoder.setJwtValidator(cognitoValidators);

        return new MultiIssuerJwtDecoder(azureDecoder, cognitoDecoder, cognitoIssuer);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        agregarComoRoles(jwt.getClaimAsStringList("roles"), authorities);
        agregarComoRoles(jwt.getClaimAsStringList("cognito:groups"), authorities);

        agregarScopes(jwt.getClaimAsString("scp"), authorities);
        agregarScopes(jwt.getClaimAsString("scope"), authorities);

        return authorities;
    }

    private void agregarComoRoles(List<String> valores, Collection<GrantedAuthority> authorities) {
        if (valores == null) return;
        valores.forEach(v -> authorities.add(new SimpleGrantedAuthority("ROLE_" + v)));
    }

    private void agregarScopes(String scopesCrudos, Collection<GrantedAuthority> authorities) {
        if (scopesCrudos == null) return;
        for (String scope : scopesCrudos.split(" ")) {
            if (!scope.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
            }
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}