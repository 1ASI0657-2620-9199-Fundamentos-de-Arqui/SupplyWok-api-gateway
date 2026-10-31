package aurora.supply_wok.gateway.security;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestAuditFilter implements WebFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestAuditFilter.class);
    private static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String id = UUID.randomUUID().toString();
        var traced = exchange.mutate().request(req -> req.headers(h -> h.set(HEADER, id))).build();
        traced.getResponse().getHeaders().set(HEADER, id);
        traced.getResponse().beforeCommit(() -> {
            log.info("request correlationId={} method={} status={}", id, traced.getRequest().getMethod(),
                    traced.getResponse().getStatusCode());
            return Mono.empty();
        });
        return chain.filter(traced);
    }
}
