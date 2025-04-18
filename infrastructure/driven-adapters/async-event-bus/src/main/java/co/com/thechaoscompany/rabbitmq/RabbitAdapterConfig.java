package co.com.thechaoscompany.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitAdapterConfig {

    @Value("${rabbit.exchange}")
    private String exchange;
    @Value("${rabbit.queue}")
    private String queue;
    @Value("${rabbit.routing-key}")
    private String routingKey;
    @Value("${rabbit.dlq.exchange}")
    private String dlqExchange;
    @Value("${rabbit.dlq.routing-key}")
    private String dlqRoutingKey;

    @Bean
    public Queue ordersQueue() {
        return QueueBuilder.durable(queue)
                .withArgument("x-dead-letter-exchange", dlqExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(ordersQueue()).to(ordersExchange()).with(routingKey);
    }
}