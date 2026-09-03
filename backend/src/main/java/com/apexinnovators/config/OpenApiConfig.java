package com.apexinnovators.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI apexOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Apex Innovators API")
                        .version("1.0.0")
                        .description("""
                                REST backend for the Apex Innovators platform: project showcase,
                                hackathon archive, team, achievements, community, events and admin.
                                Public GETs and /api/auth/* are open; member mutations require a
                                bearer access token; /api/admin/** requires the ADMIN role.
                                """))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
