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
    private int sleepSeconds = 5;
    private int dlqSleepSeconds = 15;

    public void setSleepSeconds(int sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    public void setDlqSleepSeconds(int dlqSleepSeconds) {
        this.dlqSleepSeconds = dlqSleepSeconds;
    }

    private void sleep(int seconds) throws InterruptedException {
        if (seconds > 0) {
            Thread.sleep(seconds * 1000L);
        }
    }

    @RabbitListener(queues = "${rabbit.queue}")
    public void handleMessage(String message) {
        try {
            sleep(sleepSeconds);
            Order order = objectMapper.readValue(message, Order.class);
            log.info("Received message: {}", order);
            if (order.getId() % 2 == 0) {
                throw new RuntimeException("Simulated error for even order ID");
            }
        } catch (JsonProcessingException | InterruptedException e) {
            log.error("Error processing message: {}", message, e);
        }
    }

    @RabbitListener(queues = "${rabbit.dlq.queue}")
    public void handleDeadMessage(String msg) throws InterruptedException {
        sleep(dlqSleepSeconds);
        log.warn("Message received from DLQ: {}", msg);
    }

}