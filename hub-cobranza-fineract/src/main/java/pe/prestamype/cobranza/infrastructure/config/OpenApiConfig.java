package pe.prestamype.cobranza.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cobranzaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de cobranza")
                        .description("Sincronización y consulta de órdenes de cobranza")
                        .version("0.1.0")
                        .contact(new Contact().name("Prestamype")));
    }
}
