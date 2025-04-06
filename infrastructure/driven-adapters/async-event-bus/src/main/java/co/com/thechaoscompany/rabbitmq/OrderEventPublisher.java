package co.com.thechaoscompany.rabbitmq;

import co.com.thechaoscompany.model.order.Order;
import co.com.thechaoscompany.model.order.gateways.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
@Service
public class OrderEventPublisher implements OrderRepository {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rabbit.exchange}")
    private String exchange;

    @Value("${rabbit.routing-key}")
    private String routingKey;

    @Override
    public Mono<Void> emit(Order order) {
        return Mono.fromRunnable(() -> {
            try {
                String message = objectMapper.writeValueAsString(order);
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
                log.info("Event sent: {}", message);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize event", e);
            }
        });
    }
}
