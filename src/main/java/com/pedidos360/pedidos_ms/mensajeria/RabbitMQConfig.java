package com.pedidos360.pedidos_ms.mensajeria;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Esta clase configura la conexión y los componentes necesarios para interactuar con RabbitMQ. Define un intercambio de tipo "topic" para los eventos de pedidos, un convertidor de mensajes JSON y una plantilla de RabbitMQ que utiliza el convertidor de mensajes para enviar y recibir mensajes en formato JSON.
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_PEDIDOS = "pedidos.eventos";
    public static final String ROUTING_KEY_PEDIDO_CREADO = "pedido.creado";

    @Bean
    public TopicExchange exchangePedidos() {
        return new TopicExchange(EXCHANGE_PEDIDOS, true, false);
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