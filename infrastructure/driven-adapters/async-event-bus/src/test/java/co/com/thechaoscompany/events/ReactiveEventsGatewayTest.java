package co.com.thechaoscompany.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.api.domain.DomainEventBus;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactiveEventsGatewayTest {

    @Mock
    private DomainEventBus domainEventBus;

    @Mock
    private ObjectMapper objectMapper;

    private ReactiveEventsGateway gateway;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gateway = new ReactiveEventsGateway(domainEventBus, objectMapper);
        when(domainEventBus.emit(any(CloudEvent.class))).thenReturn(Mono.empty());
    }

    @Test
    public void testEmitLogsEvent() {
        Object event = new Object() {
            @Override
            public String toString() {
                return "testEvent";
            }
        };

        when(objectMapper.valueToTree(event)).thenReturn(mock(ObjectNode.class));

        gateway.emit(event).block();

        verify(domainEventBus, times(1)).emit(any(CloudEvent.class));
    }

    @Test
    public void testEmitConstructsCloudEvent() {
        Object event = new Object() {
            public String toString() {
                return "testEvent";
            }
        };

        when(objectMapper.valueToTree(event)).thenReturn(mock(ObjectNode.class));

        gateway.emit(event).block();

        ArgumentCaptor<CloudEvent> eventCaptor = ArgumentCaptor.forClass(CloudEvent.class);
        verify(domainEventBus, times(1)).emit(eventCaptor.capture());

        CloudEvent cloudEvent = eventCaptor.getValue();
        assertEquals(ReactiveEventsGateway.SOME_EVENT_NAME, cloudEvent.getType());
        assertEquals("https://reactive-commons.org/foos", cloudEvent.getSource().toString());
    }

}
