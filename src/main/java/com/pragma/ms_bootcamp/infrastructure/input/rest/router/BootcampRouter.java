package com.pragma.ms_bootcamp.infrastructure.input.rest.router;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.RegistrationRequest;
import com.pragma.ms_bootcamp.infrastructure.input.rest.handler.BootcampHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BootcampRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/bootcamps",
                    method = RequestMethod.POST,
                    beanClass = BootcampHandler.class,
                    beanMethod = "createBootcamp",
                    operation = @Operation(
                            operationId = "createBootcamp",
                            summary = "Crear un bootcamp",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = BootcampRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Bootcamp creado")
                            }
                    )
            ),

            @RouterOperation(
                    path = "/api/v1/bootcamps",
                    method = RequestMethod.GET,
                    beanClass = BootcampHandler.class,
                    beanMethod = "listBootcamps",
                    operation = @Operation(
                            operationId = "listBootcamps",
                            summary = "Listar bootcamps",
                            parameters = {
                                    @Parameter(name = "page", description = "Numero de la pagina"),
                                    @Parameter(name = "size", description = "Cantidad de registros por pagina"),
                                    @Parameter(name = "direction", description = "Orden de los registros"),
                                    @Parameter(name = "sortBy", description = "Ordenar por")

                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Lista de bootcamps")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/bootcamps/{id}",
                    method = RequestMethod.DELETE,
                    beanClass = BootcampHandler.class,
                    beanMethod = "deleteBootcamp",
                    operation = @Operation(
                            operationId = "deleteBootcamp",
                            summary = "Eliminar bootcamp",
                            parameters = {
                                    @Parameter(name = "id", description = "Id del bootcamp a eliminar", required = true, in = ParameterIn.PATH)
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Lista de bootcamps exitosos"),
                                    @ApiResponse(responseCode = "404", description = "Bootcamp no encontrado")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/bootcamps/registration",
                    method = RequestMethod.POST,
                    beanClass = BootcampHandler.class,
                    beanMethod = "registrationToBootcamp",
                    operation = @Operation(
                            operationId = "registrationToBootcamp",
                            summary = "Registrar una persona a un bootcamp",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = RegistrationRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Persona registrada al bootcamp")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> bootcampRoutes(BootcampHandler handler) {
        return route(POST("/api/v1/bootcamps"), handler::createBootcamp)
                .andRoute(GET("/api/v1/bootcamps"), handler::listBootcamps)
                .andRoute(DELETE("/api/v1/bootcamps/{id}"), handler::deleteBootcamp)
                .andRoute(POST("/api/v1/bootcamps/registration"), handler::registrationToBootcamp);
    }
}
