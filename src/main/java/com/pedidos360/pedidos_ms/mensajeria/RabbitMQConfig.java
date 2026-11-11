package com.pedidos360.pedidos_ms.mensajeria;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configura la conexión y los componentes para RabbitMQ: un intercambio "topic" para los eventos de pedidos,
// un convertidor JSON, una RabbitTemplate y la cola donde llegan los pagos aprobados (pago.aprobado).
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PEDIDOS = "pedidos.eventos";
    public static final String ROUTING_KEY_PEDIDO_CREADO = "pedido.creado";

    public static final String QUEUE_CREAR_DESDE_PAGO = "pedidos.crear-desde-pago";
    public static final String ROUTING_KEY_PAGO_APROBADO = "pago.aprobado";

    @Bean
    public TopicExchange exchangePedidos() {
        return new TopicExchange(EXCHANGE_PEDIDOS, true, false);
    }

    @Bean
    public Queue queueCrearDesdePago() {
        return new Queue(QUEUE_CREAR_DESDE_PAGO, true);
    }

    @Bean
    public Binding bindingCrearDesdePago(Queue queueCrearDesdePago, TopicExchange exchangePedidos) {
        return BindingBuilder.bind(queueCrearDesdePago).to(exchangePedidos).with(ROUTING_KEY_PAGO_APROBADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}