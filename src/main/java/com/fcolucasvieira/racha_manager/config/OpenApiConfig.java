package com.fcolucasvieira.racha_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Racha Manager API")
                        .version("1.0.0")
                        .description("""
                                Racha Manager API provides endpoints for managing football match sessions, players, teams, queues, and match progression.
                                
                                Main features:
                                - Manage players and sessions
                                - Create balanced teams automatically
                                - Control match flow and results
                                - Rotate teams through a priority queue
                                - Redistribute players when needed
                                - Handle errors with standardized responses
                                - Explore endpoints through Swagger/OpenAPI
                                
                                Technologies:
                                Spring Boot • Java 21 • PostgreSQL • Spring Data JPA • Hibernate • Bean Validation • Lombok • Swagger/OpenAPI • JUnit 5 • Mockito
                                """)
                        .contact(new Contact()
                                .name("Lucas Vieira")
                                .email("lucas.vieira@alu.ufc.br")
                                .url("https://github.com/fcolucasvieira")
                        )
                );
    }
}
