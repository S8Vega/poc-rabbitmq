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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private OrderEventListener orderEventListener;

  private Order order;

  @BeforeEach
  void setUp() {
    order = Order.builder()
        .id(1L)
        .customerId("C123")
        .productId("P456")
        .quantity(10)
        .status("PENDING")
        .build();
    orderEventListener.setSleepSeconds(0);
    orderEventListener.setDlqSleepSeconds(0);
  }

  @Test
  void handleMessageSuccess() throws JsonProcessingException {
    // Arrange
    String message = "{\"id\":1}";
    when(objectMapper.readValue(message, Order.class)).thenReturn(order);

    // Act & Assert
    assertDoesNotThrow(() -> orderEventListener.handleMessage(message));
    verify(objectMapper).readValue(message, Order.class);
  }

  @Test
  void handleMessageSimulatedError() throws JsonProcessingException {
    // Arrange
    order.setId(2L); // Even ID triggers error
    String message = "{\"id\":2}";
    when(objectMapper.readValue(message, Order.class)).thenReturn(order);

    // Act & Assert
    // The method throws RuntimeException for even IDs
    org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
        () -> orderEventListener.handleMessage(message));

    verify(objectMapper).readValue(message, Order.class);
  }

  @Test
  void handleMessageJsonProcessingException() throws JsonProcessingException {
    // Arrange
    String message = "invalid json";
    when(objectMapper.readValue(anyString(), eq(Order.class))).thenThrow(new JsonProcessingException("Error") {
    });

    // Act & Assert
    // JsonProcessingException is caught and logged, so it doesn't throw
    assertDoesNotThrow(() -> orderEventListener.handleMessage(message));
    verify(objectMapper).readValue(message, Order.class);
  }

  @Test
  void handleDeadMessage() {
    // Arrange
    String msg = "dead message";

    // Act & Assert
    assertDoesNotThrow(() -> orderEventListener.handleDeadMessage(msg));
  }
}
