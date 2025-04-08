package co.com.thechaoscompany.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        // http://localhost:8080/actuator/prometheus
        return registry -> registry.config().commonTags("application", "poc-rabbitmq");
    }
}