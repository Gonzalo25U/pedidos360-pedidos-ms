package com.pedidos360.pedidos_ms.service;

import com.pedidos360.pedidos_ms.dto.*;
import com.pedidos360.pedidos_ms.exception.RecursoNoEncontradoException;
import com.pedidos360.pedidos_ms.mensajeria.PagoAprobadoEvent;
import com.pedidos360.pedidos_ms.mensajeria.PedidoCreadoEvent;
import com.pedidos360.pedidos_ms.mensajeria.PedidoEventPublisher;
import com.pedidos360.pedidos_ms.model.DetallePedido;
import com.pedidos360.pedidos_ms.model.Pedido;
import com.pedidos360.pedidos_ms.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;
    private final PedidoEventPublisher eventPublisher;

    /**
     * Crea el pedido a partir de un pago aprobado (evento "pago.aprobado").
     * Idempotente por buyOrder: si el evento llega repetido (reintento de pago-ms
     * o redelivery de RabbitMQ) no se crea un segundo pedido.
     */
    @Transactional
    public void crearDesdePago(PagoAprobadoEvent evento) {
        if (repository.findByBuyOrder(evento.getBuyOrder()).isPresent()) {
            return; // ya procesado
        }

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(evento.getUsuarioId());
        pedido.setEstado("PENDIENTE");
        pedido.setBuyOrder(evento.getBuyOrder());

        BigDecimal total = BigDecimal.ZERO;
        for (PagoAprobadoEvent.Item item : evento.getItems()) {
            DetallePedido detalle = new DetallePedido(
                    item.getProductoId(), item.getNombreProducto(), item.getCantidad(), item.getPrecioUnitario());
            pedido.agregarDetalle(detalle);
            total = total.add(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
        }
        pedido.setTotal(total);

        PedidoDTO guardado;
        try {
            guardado = toDTO(repository.saveAndFlush(pedido));
        } catch (DataIntegrityViolationException e) {
            return; // carrera: otro hilo ya lo creo (indice unico buy_order)
        }

        // El pedido YA quedo guardado. Publicar el evento es lo ultimo y si falla
        // no revierte el pedido (ver PedidoEventPublisher).
        eventPublisher.publicarPedidoCreado(new PedidoCreadoEvent(
                guardado.getId(),
                evento.getUsuarioId(),
                evento.getEmailUsuario(),
                guardado.getTotal(),
                guardado.getItems()
        ));
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