package co.com.thechaoscompany.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitEntryConfig {

    @Value("${rabbit.dlq.queue}")
    private String dlqQueue;
    @Value("${rabbit.dlq.exchange}")
    private String dlqExchange;
    @Value("${rabbit.dlq.routing-key}")
    private String dlqRoutingKey;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());

        return factory;
    }

    @Bean
    public Queue ordersDlqQueue() {
        return new Queue(dlqQueue, true);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(dlqExchange);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(ordersDlqQueue())
                .to(dlqExchange())
                .with(dlqRoutingKey);
    }
}
