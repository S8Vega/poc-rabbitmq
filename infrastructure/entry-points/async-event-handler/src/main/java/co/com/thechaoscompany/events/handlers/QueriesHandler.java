package co.com.thechaoscompany.events.handlers;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@Log
@AllArgsConstructor
//@EnableQueryListeners
public class QueriesHandler {
//    private final SampleUseCase sampleUseCase;


    public Mono<Object/*change for proper model*/> handleQueryA(Object query/*change for proper model*/) {
        log.log(Level.INFO, "Query received -> {0}", query); // TODO: Remove this line
//        return sampleUseCase.doSomethingReturningNoMonoVoid(query);
        return Mono.just("Response Data");
    }

}
