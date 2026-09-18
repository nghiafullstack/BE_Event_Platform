package org.example.eventplatform.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.example.eventplatform.gateway.route.RouteTable;
import org.example.eventplatform.shared.security.JwtTokenProvider;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The whole gateway in one filter: resolve which backend a path belongs to,
 * enforce a centralized JWT check (on top of, not instead of, each service's
 * own — defense in depth, and every service stays independently testable
 * without the gateway running), then transparently proxy the request.
 * Public routes here mirror exactly what each service's own SecurityConfig
 * already permits.
 */
@Component
@Slf4j
public class GatewayProxyFilter implements WebFilter, Ordered {

    private record PublicRoute(HttpMethod method, String pattern) {
    }

    private static final List<PublicRoute> PUBLIC_ROUTES = List.of(
            new PublicRoute(HttpMethod.POST, "/api/auth/login"),
            new PublicRoute(HttpMethod.POST, "/api/auth/refresh"),
            new PublicRoute(HttpMethod.POST, "/api/tenants/register"),
            new PublicRoute(HttpMethod.POST, "/api/events"),
            new PublicRoute(HttpMethod.GET, "/api/service-categories"),
            new PublicRoute(HttpMethod.GET, "/api/vendor-profiles"),
            new PublicRoute(HttpMethod.GET, "/api/vendor-profiles/**")
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtTokenProvider jwtTokenProvider;
    private final RouteTable routeTable;
    private final WebClient webClient;

    public GatewayProxyFilter(JwtTokenProvider jwtTokenProvider, RouteTable routeTable, WebClient.Builder webClientBuilder) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.routeTable = routeTable;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        RouteTable.Route route = routeTable.resolve(path);
        if (route == null) {
            return respond(exchange, HttpStatus.NOT_FOUND, "Không có route cho " + path);
        }

        if (!isPublic(request)) {
            String token = extractToken(request);
            if (token == null || !jwtTokenProvider.validateToken(token) || jwtTokenProvider.isRefreshToken(token)) {
                return respond(exchange, HttpStatus.UNAUTHORIZED, "Thiếu hoặc sai access token");
            }
        }

        return proxy(exchange, route);
    }

    private Mono<Void> proxy(ServerWebExchange exchange, RouteTable.Route route) {
        ServerHttpRequest request = exchange.getRequest();
        String query = request.getURI().getRawQuery();
        String targetUrl = route.baseUri() + request.getPath().value() + (query != null ? "?" + query : "");

        return webClient.method(request.getMethod())
                .uri(targetUrl)
                .headers(headers -> {
                    headers.addAll(request.getHeaders());
                    headers.remove(HttpHeaders.HOST);
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                })
                .body(BodyInserters.fromDataBuffers(request.getBody()))
                .exchangeToMono(clientResponse -> {
                    exchange.getResponse().setStatusCode(clientResponse.statusCode());
                    HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
                    responseHeaders.addAll(clientResponse.headers().asHttpHeaders());
                    responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
                    return exchange.getResponse().writeWith(clientResponse.bodyToFlux(DataBuffer.class));
                })
                .onErrorResume(ex -> {
                    log.error("Proxy error forwarding {} to {}", request.getPath(), targetUrl, ex);
                    return respond(exchange, HttpStatus.BAD_GATEWAY, "Không gọi được service phía sau");
                });
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getPath().value();
        return PUBLIC_ROUTES.stream().anyMatch(r ->
                r.method().equals(request.getMethod()) && pathMatcher.match(r.pattern(), path));
    }

    private String extractToken(ServerHttpRequest request) {
        String bearer = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        var cookie = request.getCookies().getFirst("access_token");
        return cookie != null ? cookie.getValue() : null;
    }

    private Mono<Void> respond(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}"
                .formatted(status.value(), status.getReasonPhrase(), message);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
