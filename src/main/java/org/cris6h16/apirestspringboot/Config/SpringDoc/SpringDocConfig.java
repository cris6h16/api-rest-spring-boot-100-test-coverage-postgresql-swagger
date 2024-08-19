package org.cris6h16.apirestspringboot.Config.SpringDoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notes App REST API - Basic Authentication")
                        .description("A simple Notes App REST API with basic authentication, this is not a RESTful API yet, This one is lacking the explicit response marking if those are cacheable or not.")
                        .version("v1.0")
                        .contact(new Contact().name("Cristian").email("cristianmherrera21@gmail.com").url("https://github.com/cris6h16"))
                        .license(new License().name("AGPL-3.0 license").url("https://github.com/cris6h16/api-rest-spring-boot-100-test-coverage-postgresql-swagger/blob/main/LICENSE"))
                        .termsOfService("https://www.google.com/"))

                .externalDocs(new ExternalDocumentation()
                        .description("Repository Docs")
                        .url("https://github.com/cris6h16/api-rest-spring-boot-100-test-coverage-postgresql-swagger"))


                .servers(List.of(
                        new Server().description("LOCAL").url("http://localhost:8080")
//                        new Server().description("env1").url("http://helloword:8081"),
//                        new Server().description("env2").url("http://exampleserver:8000")
                ))

                .tags(List.of(
                        new Tag().name("Note Endpoints").description("Operations that authenticated users can do"),
                        new Tag().name("Admin User Endpoints").description("Operations that admins can do"),
                        new Tag().name("Public User Endpoints").description("Operations that anyone can do"),
                        new Tag().name("Authenticated User Endpoints").description("Operations that authenticated users can do")
                ))

                .components(
                        new Components().addSecuritySchemes("basicAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic"))
                );

    }
}
