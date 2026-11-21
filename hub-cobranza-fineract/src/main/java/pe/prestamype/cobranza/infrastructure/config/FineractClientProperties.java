package pe.prestamype.cobranza.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fineract")
public record FineractClientProperties(
        String baseUrl,
        String tenantId,
        String username,
        String password,
        int connectTimeoutMs,
        int readTimeoutMs,
        boolean insecureSsl
) {
}
