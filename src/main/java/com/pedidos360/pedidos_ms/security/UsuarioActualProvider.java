package com.pedidos360.pedidos_ms.security;

// Esta interfaz define los métodos necesarios para obtener información sobre el usuario actualmente autenticado en el sistema. Permite obtener el ID del usuario, verificar si tiene el rol de administrador y obtener su correo electrónico, lo cual es útil para notificaciones relacionadas con pedidos.
public interface UsuarioActualProvider {
    String obtenerUsuarioId();

    /** true si el usuario autenticado tiene el rol Admin (puede ver todos los pedidos). */
    boolean esAdmin();

    /** Email del usuario, usado para notificarle la confirmacion del pedido. */
    String obtenerEmail();
}