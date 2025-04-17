package co.com.thechaoscompany.api;

import co.com.thechaoscompany.model.order.Order;
import co.com.thechaoscompany.usecase.order.OrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiRest {
    private final OrderUseCase useCase;

    @PostMapping(path = "/publish")
    public Mono<String> commandName(@RequestBody Order order) {
        order.setId(Math.round(Math.random() * 10000));
        order.setDate(LocalDateTime.now());
        order.setStatus("PENDING");
        return useCase.publish(order)
                .then(Mono.just("Order published successfully"))
                .onErrorResume(e -> Mono.just("Failed to publish order: " + e.getMessage()));
    }
}
