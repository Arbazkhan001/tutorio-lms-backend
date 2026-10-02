package com.affycloud.TutorioBackend.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // Add Bearer Token Security Scheme
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .in(SecurityScheme.In.HEADER);

        return new OpenAPI()
                .addServersItem(new Server()
                        .url("https://api.tutorio.in")
//                        .url("http://192.168.1.30:8070")
                        .description("Production Server"))
                .components(
                        new Components()
                                .addSecuritySchemes("BearerAuth", bearerAuth)
                )
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .info(new Info()
                        .title("Your API Documentation")
                        .version("1.0.0"));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {

        return openApi -> openApi.getPaths().values()
                .forEach(pathItem -> pathItem.readOperations()
                        .forEach(operation -> {

                            // Add server for each operation
                            Server server = new Server().url("https://api.tutorio.in");
//                            Server server = new Server().url("http://192.168.1.30:8070");
                            operation.addServersItem(server);

                            // Add security to each API operation
                            operation.addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
                        }));
    }
}
