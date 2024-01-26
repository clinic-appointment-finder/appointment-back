package cl.hcs.finder.appointmentback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clinic/indisa/external")
@Tag(name = "Indisa External Controller", description = "Endpoints para las invocaciones scrapping de la clinica Indisa")
public class IndisaExternalController {

    @Autowired
    private final IndisaServiceInvoker externalServiceInvoker;

    public IndisaExternalController(IndisaServiceInvoker externalServiceInvoker) {
        this.externalServiceInvoker = externalServiceInvoker;
    }

    @Operation(summary = "Buscar cita en la clínica", description = "Busca una cita para un doctor con la API de la clínica Indisa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = IndisaCalendarOutputModel.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/appointments")
    public Mono<IndisaCalendarOutputModel> invokeExternalService(          
            @Parameter(description = "ID de la Especialidad médica", example = "226", required = true) @RequestParam String specialityID,
            @Parameter(description = "ID del doctor asociada a la sucursal ", example = "14655", required = true) @RequestParam String doctorID,
            @Parameter(description = "sucursal de la Clínica", example = "PROVIDENCIA", required = true) @RequestParam String office) {
        return externalServiceInvoker
                .invokeIndisaCalendar(
                        new IndisaCalendarInputModel(specialityID, doctorID, office));
    }

    @Operation(summary = "Buscar sucursales", description = "Busca las sucursales disponibles de la clínica Indisa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = IndisaCalendarOutputModel.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/offices")
    public ResponseEntity<List<String>> invokeExternalServiceOffice() {
        List<String> result = externalServiceInvoker
                .invokeIndisaOffice();
        if (result.isEmpty())
            new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
 
}
