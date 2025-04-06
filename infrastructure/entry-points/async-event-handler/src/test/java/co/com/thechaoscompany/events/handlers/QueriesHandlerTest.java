package co.com.thechaoscompany.events.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class QueriesHandlerTest {
    private QueriesHandler queriesHandler;

    @BeforeEach
    void setUp() {
        queriesHandler = new QueriesHandler();
    }

    @Test
    void queriesHandlerTest() {
    // Enviar una consulta con datos específicos
        String query = "Data";
        
        // Verificar que el valor retornado sea el esperado
        StepVerifier.create(queriesHandler.handleQueryA(query))
            .expectNext("Response Data") // Verifica que el valor recibido sea "Response Data"
            .expectComplete()  // Verifica que el Mono se complete
            .verify();
    }
}
