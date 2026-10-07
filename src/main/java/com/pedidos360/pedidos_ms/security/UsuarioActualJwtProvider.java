package com.pedidos360.pedidos_ms.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
 
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
@Component
@Profile("!local")
public class UsuarioActualJwtProvider implements UsuarioActualProvider {
 
    private static final Logger log = LoggerFactory.getLogger(UsuarioActualJwtProvider.class);
 
    /** Dominio del Hosted UI de Cognito: ahi vive el endpoint /oauth2/userInfo. */
    @Value("${cognito.domain:https://us-east-1a8woifduy.auth.us-east-1.amazoncognito.com}")
    private String cognitoDomain;
 
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
 
    /** sub -> email. Evita llamar a Cognito en cada pedido del mismo usuario. */
    private final Map<String, String> cacheEmailCognito = new ConcurrentHashMap<>();
 
    @Override
    public String obtenerUsuarioId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }
 
    @Override
    public boolean esAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_Admin"));
    }
 
    @Override
    public String obtenerEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Azure AD no siempre usa el mismo claim para el email: las cuentas
        // personales suelen traer "email"; las organizacionales, "preferred_username"
        // (que normalmente ES su correo/UPN); como ultimo respaldo, "upn".
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("preferred_username");
        }
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("upn");
        }
        // El ACCESS token de Cognito no incluye el email (solo el ID token lo
        // trae). Para esos tokens se lo pedimos a Cognito (/oauth2/userInfo).
        if ((email == null || email.isBlank()) && esTokenDeCognito(jwt)) {
            email = emailDesdeCognito(jwt);
        }
        return email;
    }
 
    private boolean esTokenDeCognito(Jwt jwt) {
        String iss = jwt.getClaimAsString("iss");
        return iss != null && iss.contains("cognito-idp");
    }
 
    private String emailDesdeCognito(Jwt jwt) {
        String sub = jwt.getSubject();
        String cacheado = cacheEmailCognito.get(sub);
        if (cacheado != null) {
            return cacheado;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(cognitoDomain.replaceAll("/$", "") + "/oauth2/userInfo"))
                    .timeout(Duration.ofSeconds(4))
                    .header("Authorization", "Bearer " + jwt.getTokenValue())
                    .GET()
                    .build();
            HttpResponse<String> respuesta = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                log.warn("Cognito userInfo respondio {} (el pedido sigue, sin correo)", respuesta.statusCode());
                return null;
            }
            JsonNode email = mapper.readTree(respuesta.body()).get("email");
            if (email != null && !email.asText().isBlank()) {
                cacheEmailCognito.put(sub, email.asText());
                return email.asText();
            }
        } catch (Exception e) {
            // Nunca debe romper el checkout: sin email simplemente no hay correo.
            log.warn("No se pudo obtener el email desde Cognito: {}", e.getMessage());
        }
        return null;
    }
}