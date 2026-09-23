package com.pedidos360.pedidos_ms.security;


import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
public class UsuarioActualJwtProvider implements UsuarioActualProvider {

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
        return email;
    }
}