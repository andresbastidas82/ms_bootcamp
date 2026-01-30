package com.pragma.ms_bootcamp.infrastructure.input.rest.router;

import com.pragma.ms_bootcamp.infrastructure.input.rest.handler.BootcampHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BootcampRouter {

    @Bean
    public RouterFunction<ServerResponse> bootcampRoutes(BootcampHandler handler) {
        return route(POST("/api/v1/bootcamps"), handler::createBootcamp)
                .andRoute(GET("/api/v1/bootcamps"), handler::listBootcamps)
                .andRoute(DELETE("/api/v1/bootcamps/{id}"), handler::deleteBootcamp);
    }
}
