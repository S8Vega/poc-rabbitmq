package co.com.thechaoscompany.usecase.order;

import co.com.thechaoscompany.model.order.Order;
import co.com.thechaoscompany.model.order.gateways.OrderRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrderUseCase {
    private final OrderRepository repository;

    public Mono<Void> publish(Order order) {
        return repository.publish(order);
    }
}
