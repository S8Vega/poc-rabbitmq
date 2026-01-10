package co.com.thechaoscompany.rabbitmq;

import co.com.thechaoscompany.model.order.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderEventListener listener;

    private Order validOrder;
    private String validOrderJson;

    @BeforeEach
    void setUp() {
        validOrder = Order.builder()
                .id(1L)
                .customerId("CUST001")
                .productId("PROD123")
                .quantity(5)
                .status("PENDING")
                .date(LocalDateTime.now())
                .build();

        validOrderJson = "{\"id\":1,\"customerId\":\"CUST001\",\"productId\":\"PROD123\",\"quantity\":5,\"status\":\"PENDING\"}";
    }

    @Test
    void handleMessage_withOddOrderId_shouldProcessSuccessfully() throws JsonProcessingException {
        // Given
        when(objectMapper.readValue(eq(validOrderJson), eq(Order.class))).thenReturn(validOrder);

        // When
        listener.handleMessage(validOrderJson);

        // Then
        verify(objectMapper).readValue(eq(validOrderJson), eq(Order.class));
    }

    @Test
    void handleMessage_withEvenOrderId_shouldThrowRuntimeException() throws JsonProcessingException {
        // Given
        Order evenOrderIdOrder = validOrder.toBuilder().id(2L).build();
        String evenOrderJson = "{\"id\":2,\"customerId\":\"CUST001\",\"productId\":\"PROD123\",\"quantity\":5,\"status\":\"PENDING\"}";
        when(objectMapper.readValue(eq(evenOrderJson), eq(Order.class))).thenReturn(evenOrderIdOrder);

        // When/Then
        assertThrows(RuntimeException.class, () -> listener.handleMessage(evenOrderJson));

        verify(objectMapper).readValue(eq(evenOrderJson), eq(Order.class));
        // RuntimeException is thrown to trigger DLQ routing in RabbitMQ
    }

    @Test
    void handleMessage_withInvalidJson_shouldHandleJsonProcessingException() throws JsonProcessingException {
        // Given
        String invalidJson = "{invalid json}";
        when(objectMapper.readValue(eq(invalidJson), eq(Order.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // When
        listener.handleMessage(invalidJson);

        // Then
        verify(objectMapper).readValue(eq(invalidJson), eq(Order.class));
        // Exception is caught and logged, no exception propagated
    }

    @Test
    void handleMessage_withNullMessage_shouldHandleException() throws JsonProcessingException {
        // Given
        when(objectMapper.readValue((String) isNull(), eq(Order.class)))
                .thenThrow(new JsonProcessingException("Null message") {});

        // When
        listener.handleMessage(null);

        // Then
        verify(objectMapper).readValue((String) isNull(), eq(Order.class));
    }

    @Test
    void handleDeadMessage_shouldProcessDlqMessage() throws InterruptedException {
        // Given
        String dlqMessage = "{\"id\":2,\"customerId\":\"CUST001\",\"productId\":\"PROD123\"}";

        // When
        listener.handleDeadMessage(dlqMessage);

        // Then
        // No exception should be thrown and message should be logged
        // This test verifies the method executes without errors
    }

    @Test
    void handleDeadMessage_withNullMessage_shouldNotThrowException() throws InterruptedException {
        // When
        listener.handleDeadMessage(null);

        // Then
        // No exception should be thrown
    }

    @Test
    void handleDeadMessage_withEmptyMessage_shouldNotThrowException() throws InterruptedException {
        // When
        listener.handleDeadMessage("");

        // Then
        // No exception should be thrown
    }
}
