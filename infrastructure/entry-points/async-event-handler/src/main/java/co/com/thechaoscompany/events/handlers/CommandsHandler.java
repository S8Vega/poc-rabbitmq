package co.com.thechaoscompany.events.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.reactivecommons.api.domain.Command;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@Log
@AllArgsConstructor
//@EnableCommandListeners
public class CommandsHandler {
//    private final SampleUseCase sampleUseCase;

    public Mono<Void> handleCommandA(Command<Object/*change for proper model*/> command) {
        log.log(Level.INFO, "Command received: {0} -> {1}", new Object[]{command.getName(), command.getData()}); // TODO: Remove this line
//        return sampleUseCase.doSomething(command.getData());
        return Mono.empty();
    }

}
