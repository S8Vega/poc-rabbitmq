package co.com.thechaoscompany.model.order.gateways;

import co.com.thechaoscompany.model.order.Order;
import reactor.core.publisher.Mono;

public interface OrderRepository {
    Mono<Void> emit(Order order);
}
