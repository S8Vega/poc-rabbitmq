package co.com.thechaoscompany.events;

import co.com.bancolombia.events.handlers.CommandsHandler;
import co.com.bancolombia.events.handlers.EventsHandler;
import co.com.bancolombia.events.handlers.QueriesHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivecommons.async.api.HandlerRegistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HandlerRegistryConfigurationTest {

    EventsHandler eventsHandler;
    CommandsHandler commandsHandler;
    QueriesHandler queriesHandler;

    @BeforeEach
    void setUp() {
        eventsHandler = new EventsHandler();
        commandsHandler = new CommandsHandler();
        queriesHandler = new QueriesHandler();
    }

    @Test
    void testHandlerRegistry() {
        HandlerRegistryConfiguration handlerRegistryConfiguration = new HandlerRegistryConfiguration();
        HandlerRegistry handlerRegistry = handlerRegistryConfiguration.handlerRegistry(commandsHandler, eventsHandler, queriesHandler);

        assertNotNull(handlerRegistry);
    }
}
