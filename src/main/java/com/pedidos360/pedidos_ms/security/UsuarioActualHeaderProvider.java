package com.pedidos360.pedidos_ms.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@Profile("local")
@RequestScope
public class UsuarioActualHeaderProvider implements UsuarioActualProvider {

    private final HttpServletRequest request;

    public UsuarioActualHeaderProvider(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String obtenerUsuarioId() {
        String header = request.getHeader("X-User-Id");
        return (header != null && !header.isBlank()) ? header : "usuario-anonimo-local";
    }

    @Override
    public boolean esAdmin() {
        String rol = request.getHeader("X-User-Role");
        return "Admin".equalsIgnoreCase(rol);
    }

    @Override
    public String obtenerEmail() {
        String header = request.getHeader("X-User-Email");
        return (header != null && !header.isBlank()) ? header : "usuario-local@ejemplo.com";
    }
}