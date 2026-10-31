package aurora.supply_wok.gateway.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import java.util.Map;

@RestController
public class UnavailableEndpoint {
    @RequestMapping("/internal/dependency-unavailable")
    public ResponseEntity<Map<String, String>> unavailable(ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "code", "DEPENDENCY_UNAVAILABLE", "correlationId",
                exchange.getRequest().getHeaders().getFirst("X-Correlation-Id")));
    }
}
