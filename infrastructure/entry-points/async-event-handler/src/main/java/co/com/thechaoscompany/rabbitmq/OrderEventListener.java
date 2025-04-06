package co.com.thechaoscompany.rabbitmq;

import co.com.thechaoscompany.model.order.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbit.queue}")
    public void handleMessage(String message) {
        try {
            Thread.sleep(5000);
            Order order = objectMapper.readValue(message, Order.class);
            log.info("Received message: {}", order);
            if (order.getId() % 2 == 0) {
                throw new RuntimeException("Simulated error for even order ID");
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing message: {}", message, e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}