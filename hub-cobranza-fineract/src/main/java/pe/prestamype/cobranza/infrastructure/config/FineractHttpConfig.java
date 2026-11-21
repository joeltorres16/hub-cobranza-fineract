package pe.prestamype.cobranza.infrastructure.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import pe.prestamype.cobranza.application.port.out.LoteCobranzaRepositoryPort;
import pe.prestamype.cobranza.application.port.out.OrdenCobranzaRepositoryPort;
import pe.prestamype.cobranza.application.port.out.PrestamoFineractPort;
import pe.prestamype.cobranza.application.service.SincronizacionCobranzaService;
import pe.prestamype.cobranza.domain.service.ReglasSincronizacionService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(FineractClientProperties.class)
public class FineractHttpConfig {

    @Bean
    public Clock clockUtc() {
        return Clock.systemUTC();
    }

    @Bean
    public ReglasSincronizacionService reglasSincronizacionService() {
        return new ReglasSincronizacionService();
    }

    @Bean
    public SincronizacionCobranzaService sincronizacionCobranzaService(
            PrestamoFineractPort prestamoFineractPort,
            LoteCobranzaRepositoryPort loteRepositoryPort,
            OrdenCobranzaRepositoryPort ordenRepositoryPort,
            ReglasSincronizacionService reglasSincronizacionService,
            Clock clockUtc
    ) {
        return new SincronizacionCobranzaService(
                prestamoFineractPort,
                loteRepositoryPort,
                ordenRepositoryPort,
                reglasSincronizacionService,
                clockUtc
        );
    }

    @Bean
    public RestClient fineractRestClient(FineractClientProperties props) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(props.connectTimeoutMs()))
                .setResponseTimeout(Timeout.ofMilliseconds(props.readTimeoutMs()))
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory;
        if (props.insecureSsl()) {
            CloseableHttpClient insecureClient = createInsecureHttpClient(requestConfig);
            requestFactory = new HttpComponentsClientHttpRequestFactory(insecureClient);
        } else {
            CloseableHttpClient standardClient = HttpClients.custom()
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            requestFactory = new HttpComponentsClientHttpRequestFactory(standardClient);
        }

        String authRaw = props.username() + ":" + props.password();
        String basicToken = Base64.getEncoder().encodeToString(authRaw.getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicToken)
                .defaultHeader("Fineract-Platform-TenantId", props.tenantId())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private CloseableHttpClient createInsecureHttpClient(RequestConfig requestConfig) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContexts.custom().build();
            sslContext.init(null, trustAllCerts, null);

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    NoopHostnameVerifier.INSTANCE
            );

            var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            return HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo configurar el cliente HTTP para Fineract", ex);
        }
    }
}
