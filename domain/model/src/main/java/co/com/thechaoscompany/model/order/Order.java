package co.com.thechaoscompany.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Order {
    private long id;
    private String customerId;
    private String productId;
    private int quantity;
    private String status;
    private LocalDateTime date;
}
