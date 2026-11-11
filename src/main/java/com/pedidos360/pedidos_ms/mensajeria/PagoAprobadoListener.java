package com.pedidos360.pedidos_ms.mensajeria;

import com.pedidos360.pedidos_ms.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagoAprobadoListener {

    private final PedidoService pedidoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CREAR_DESDE_PAGO)
    public void onPagoAprobado(PagoAprobadoEvent evento) {
        log.info("pago.aprobado recibido: buyOrder={} usuario={}", evento.getBuyOrder(), evento.getUsuarioId());
        pedidoService.crearDesdePago(evento);
    }
}