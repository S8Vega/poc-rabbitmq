package co.com.thechaoscompany.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.async.api.DirectAsyncGateway;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonCloudEventData;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReactiveDirectAsyncGatewayTest {

    @Mock
    private DirectAsyncGateway directAsyncGateway;
    @Mock
    private ObjectMapper objectMapper;

    private ReactiveDirectAsyncGateway gateway;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        gateway = new ReactiveDirectAsyncGateway(directAsyncGateway, objectMapper);
    }

    @Test
    public void testRunRemoteJobSendsCommand() throws Exception {
        Object command = new Object() {
            public String toString() {
                return "testCommand";
            }
        };
        when(objectMapper.valueToTree(command)).thenReturn(mock(ObjectNode.class));
        when(directAsyncGateway.sendCommand(any(CloudEvent.class), any(String.class))).thenReturn(Mono.empty());

        gateway.runRemoteJob(command).block();

        ArgumentCaptor<CloudEvent> eventCaptor = ArgumentCaptor.forClass(CloudEvent.class);
        verify(directAsyncGateway, times(1)).sendCommand(eventCaptor.capture(), eq(ReactiveDirectAsyncGateway.TARGET_NAME));
        CloudEvent cloudEvent = eventCaptor.getValue();
    }

    @Test
    public void testRequestForRemoteDataSendsQuery() throws JsonProcessingException {
        Object query = new Object() {
            public String toString() {
                return "testQuery";
            }
        };
        ObjectNode mockNode = mock(ObjectNode.class);
        when(objectMapper.valueToTree(query)).thenReturn(mockNode);
        when(objectMapper.createObjectNode()).thenReturn(new ObjectMapper().createObjectNode());

        CloudEvent mockCloudEvent = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create("https://spring.io/foos"))
                .withType(ReactiveDirectAsyncGateway.SOME_QUERY_NAME)
                .withTime(OffsetDateTime.now())
                .withData("application/json", JsonCloudEventData.wrap(objectMapper.createObjectNode().put("key", "value")))
                .build();

        when(directAsyncGateway.requestReply(any(CloudEvent.class), any(String.class), eq(CloudEvent.class)))
                .thenReturn(Mono.just(mockCloudEvent));

        gateway.requestForRemoteData(query).block();

        ArgumentCaptor<CloudEvent> eventCaptor = ArgumentCaptor.forClass(CloudEvent.class);
        verify(directAsyncGateway, times(1)).requestReply(eventCaptor.capture(), eq(ReactiveDirectAsyncGateway.TARGET_NAME), eq(CloudEvent.class));
        CloudEvent cloudEvent = eventCaptor.getValue();
    }
}
