package com.pedidos360.pedidos_ms.mensajeria;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PedidoEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarPedidoCreado(PedidoCreadoEvent evento) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PEDIDOS,
                    RabbitMQConfig.ROUTING_KEY_PEDIDO_CREADO,
                    evento
            );
            log.info("Evento pedido.creado publicado para el pedido {}", evento.getPedidoId());
        } catch (Exception e) {
            // Si RabbitMQ esta caido, el pedido YA se guardo en la base de datos
            // (ver PedidoService) - no queremos que el checkout falle solo porque
            // las notificaciones/descuento de stock no se pudieron encolar. Se
            // registra el error para revisarlo despues.
            log.error("No se pudo publicar el evento pedido.creado para el pedido {}: {}",
                    evento.getPedidoId(), e.getMessage());
        }
    }
}
