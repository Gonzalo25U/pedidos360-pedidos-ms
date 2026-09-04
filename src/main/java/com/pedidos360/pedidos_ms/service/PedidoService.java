package com.pedidos360.pedidos_ms.service;

import com.pedidos360.pedidos_ms.dto.*;
import com.pedidos360.pedidos_ms.exception.RecursoNoEncontradoException;
import com.pedidos360.pedidos_ms.model.DetallePedido;
import com.pedidos360.pedidos_ms.model.Pedido;
import com.pedidos360.pedidos_ms.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;

    @Transactional
    public PedidoDTO crear(String usuarioId, CrearPedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setEstado("PENDIENTE");

        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoRequest item : request.getItems()) {
            DetallePedido detalle = new DetallePedido(
                    item.getProductoId(), item.getNombreProducto(), item.getCantidad(), item.getPrecioUnitario());
            pedido.agregarDetalle(detalle);
            total = total.add(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
        }
        pedido.setTotal(total);

        return toDTO(repository.save(pedido));
    }

    /** Lista SOLO los pedidos del usuario indicado (aislamiento por usuario). */
    public List<PedidoDTO> listarPropios(String usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream().map(this::toDTO).toList();
    }

    /** Un usuario solo puede ver el detalle de SU PROPIO pedido. */
    public PedidoDTO obtenerPropio(String usuarioId, Long pedidoId) {
        Pedido pedido = repository.findByIdAndUsuarioId(pedidoId, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + pedidoId));
        return toDTO(pedido);
    }

    /** Solo para el endpoint de Admin: ve los pedidos de TODOS los usuarios. */
    public List<PedidoDTO> listarTodos() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    private PedidoDTO toDTO(Pedido pedido) {
        List<ItemPedidoDTO> items = pedido.getDetalles().stream()
                .map(d -> new ItemPedidoDTO(d.getProductoId(), d.getNombreProducto(), d.getCantidad(), d.getPrecioUnitario()))
                .toList();
        return new PedidoDTO(pedido.getId(), pedido.getUsuarioId(), pedido.getEstado(),
                pedido.getTotal(), pedido.getCreadoEn(), items);
    }
}
