package com.pedidos360.pedidos_ms.mensajeria;

import com.pedidos360.pedidos_ms.dto.ItemPedidoDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Esta clase representa el evento de creación de un pedido que se enviará a través de RabbitMQ. Contiene información relevante sobre el pedido, como su ID, el ID del usuario que lo creó, el correo electrónico del usuario, el total del pedido y la lista de ítems del pedido.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoCreadoEvent {
    private Long pedidoId;
    private String usuarioId;
    private String emailUsuario;
    private BigDecimal total;
    private List<ItemPedidoDTO> items;
}