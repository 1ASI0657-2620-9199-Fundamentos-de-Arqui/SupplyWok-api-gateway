package aurora.supply_wok.gateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RouteConfigurationTest {

    private static HttpServer mockInventoryServer;
    private static int mockInventoryPort;
    private static final AtomicBoolean inventoryEndpointCalled = new AtomicBoolean(false);

    @BeforeAll
    static void startMockServer() throws IOException {
        mockInventoryServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        mockInventoryPort = mockInventoryServer.getAddress().getPort();

        mockInventoryServer.createContext("/api/v1/supplies", exchange -> {
            inventoryEndpointCalled.set(true);
            byte[] response = "[{\"id\":1,\"name\":\"Test Supply\"}]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        mockInventoryServer.start();
    }

    @AfterAll
    static void stopMockServer() {
        if (mockInventoryServer != null) {
            mockInventoryServer.stop(0);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("INVENTORY_SERVICE_URL", () -> "http://127.0.0.1:" + mockInventoryPort);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void shouldRouteSuppliesToInventoryService() {
        webTestClient.get()
                .uri("/api/v1/supplies")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody(String.class)
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).contains("Test Supply");
                });

        assertThat(inventoryEndpointCalled.get()).isTrue();
    }

    @Test
    void shouldVerifyRoutesAreConfigured() {
        var routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();
        assertThat(routes)
                .extracting("id")
                .contains(
                        "iam-service-authentication",
                        "iam-service-users",
                        "iam-service-profiles",
                        "inventory-service-supplies",
                        "operations-service-tables",
                        "operations-service-comandas",
                        "operations-service-sensors",
                        "operations-service-restaurant-alerts",
                        "operations-service-supplier-alerts",
                        "purchasing-service-suppliers",
                        "purchasing-service-purchase-orders"
                );
    }
}
