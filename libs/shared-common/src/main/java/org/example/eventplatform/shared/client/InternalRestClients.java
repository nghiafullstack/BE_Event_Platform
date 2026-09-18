package org.example.eventplatform.shared.client;

import org.example.eventplatform.shared.security.InternalServiceHeaders;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds a {@link RestClient} pre-wired with {@code X-Internal-Token} for
 * service-to-service calls against {@code /api/internal/**}.
 *
 * <p>Every service serializes its API responses as snake_case
 * ({@code spring.jackson.property-naming-strategy=SNAKE_CASE}), but that
 * property only configures the Boot-managed MVC {@code ObjectMapper} — a
 * plain {@code RestClient.builder().build()} falls back to Spring's default
 * (camelCase) converter, which silently leaves multi-word fields null
 * instead of throwing. Wiring the same naming strategy here keeps internal
 * DTOs (record field names) deserializing correctly.</p>
 */
public final class InternalRestClients {

    private InternalRestClients() {
    }

    public static RestClient create(String baseUrl, String internalToken) {
        JsonMapper objectMapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        JacksonJsonHttpMessageConverter jsonConverter = new JacksonJsonHttpMessageConverter(objectMapper);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof JacksonJsonHttpMessageConverter);
                    converters.add(jsonConverter);
                });
        if (StringUtils.hasText(internalToken)) {
            builder.defaultHeader(InternalServiceHeaders.TOKEN, internalToken);
        }
        return builder.build();
    }
}
