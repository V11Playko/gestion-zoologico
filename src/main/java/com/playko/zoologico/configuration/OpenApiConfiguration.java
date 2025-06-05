package com.playko.zoologico.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.playko.zoologico.constants.SwaggerConstants.SWAGGER_DESCRIPTION_MESSAGE;
import static com.playko.zoologico.constants.SwaggerConstants.SWAGGER_LICENSE_NAME_MESSAGE;
import static com.playko.zoologico.constants.SwaggerConstants.SWAGGER_LICENSE_URL_MESSAGE;
import static com.playko.zoologico.constants.SwaggerConstants.SWAGGER_TERMS_OF_SERVICE_MESSAGE;
import static com.playko.zoologico.constants.SwaggerConstants.SWAGGER_TITLE_MESSAGE;
import static com.playko.zoologico.constants.SwaggerConstants.SWAGGER_VERSION_MESSAGE;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI openApiConfig() {
        Schema<?> mapSchema = new Schema<Map<String, String>>()
                .addProperty("message", new StringSchema().example("string"));
        Schema<?> errorSchema = new Schema<Map<String, String>>()
                .addProperty("error", new StringSchema().example("string"));
        return new OpenAPI()
                .info(new Info()
                        .title(SWAGGER_TITLE_MESSAGE)
                        .description(SWAGGER_DESCRIPTION_MESSAGE)
                        .version(SWAGGER_VERSION_MESSAGE)
                        .license(new License().name(SWAGGER_LICENSE_NAME_MESSAGE).url(SWAGGER_LICENSE_URL_MESSAGE))
                        .termsOfService(SWAGGER_TERMS_OF_SERVICE_MESSAGE))
                .components(new Components()
                        .addSchemas("Map", mapSchema)
                        .addSchemas("Error", errorSchema));

    }
}
