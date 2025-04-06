package co.com.thechaoscompany.events.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.reactivecommons.api.domain.DomainEvent;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@Log
@AllArgsConstructor
//@EnableEventListeners
public class EventsHandler {
//    private final SampleUseCase sampleUseCase;


    public Mono<Void> handleEventA(DomainEvent<Object/*change for proper model*/> event) {
        log.log(Level.INFO, "Event received: {0} -> {1}", new Object[]{event.getName(), event.getData()}); // TODO: Remove this line
//        return sampleUseCase.doSomething(event.getData());
        return Mono.empty();
    }
}
