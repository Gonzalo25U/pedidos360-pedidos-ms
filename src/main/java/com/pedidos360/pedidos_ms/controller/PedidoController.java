package com.pedidos360.pedidos_ms.controller;

import com.pedidos360.pedidos_ms.dto.PedidoDTO;
import com.pedidos360.pedidos_ms.exception.AccesoNoPermitidoException;
import com.pedidos360.pedidos_ms.security.UsuarioActualProvider;
import com.pedidos360.pedidos_ms.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Los pedidos YA NO se crean por HTTP: los crea el consumidor del evento
 * "pago.aprobado" (PagoAprobadoListener) cuando Webpay aprueba el pago.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService service;
    private final UsuarioActualProvider usuarioActualProvider;

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> misPedidos() {
        return ResponseEntity.ok(service.listarPropios(usuarioActualProvider.obtenerUsuarioId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPropio(usuarioActualProvider.obtenerUsuarioId(), id));
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<PedidoDTO>> todos() {
        if (!usuarioActualProvider.esAdmin()) {
            throw new AccesoNoPermitidoException("Solo un Admin puede ver todos los pedidos");
        }
        return ResponseEntity.ok(service.listarTodos());
    }
}