package com.pedidos360.pedidos_ms.security;

public interface UsuarioActualProvider {
    String obtenerUsuarioId();

    /** true si el usuario autenticado tiene el rol Admin (puede ver todos los pedidos). */
    boolean esAdmin();
}
