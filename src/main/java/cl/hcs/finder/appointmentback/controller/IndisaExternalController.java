package cl.hcs.finder.appointmentback.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.hcs.finder.appointmentback.model.GenericOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
        public IndisaCalendarOutputModel invokeExternalService(
                        @Parameter(description = "ID de la Especialidad médica", example = "226", required = true) @RequestParam String codeSpeciality,
                        @Parameter(description = "ID del doctor asociada a la sucursal ", example = "14655", required = true) @RequestParam String codeDoctor,
                        @Parameter(description = "sucursal de la Clínica", example = "PROVIDENCIA", required = true) @RequestParam String office,
                        @Parameter(description = "ID de la previsión", example = "67", required = true) @RequestParam String codePrevision) {
                return externalServiceInvoker
                                .invokeIndisaCalendar(
                                                new IndisaCalendarInputModel(codeSpeciality, codeDoctor, office,
                                                                codePrevision));
        }

        @Operation(summary = "Buscar sucursales", description = "Busca las sucursales disponibles de la clínica Indisa")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", content = {
                                        @Content(schema = @Schema(implementation = IndisaCalendarOutputModel.class), mediaType = "application/json") }),
                        @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
                        @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
        @GetMapping("/offices")
        public ResponseEntity<List<String>> invokeExternalServiceOffice(
                        @Parameter(description = "ID de la previsión", example = "67", required = true) @RequestParam String codePrevision) {
                List<String> result = externalServiceInvoker
                                .invokeIndisaOffice(codePrevision);
                if (result.isEmpty())
                        new ResponseEntity<>(HttpStatus.NOT_FOUND);
                return new ResponseEntity<>(result, HttpStatus.OK);
        }

        @Operation(summary = "lista de previsión", description = "traer todas las previsiones o seguros de salud chilenas")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", content = {
                                        @Content(schema = @Schema(implementation = List.class), mediaType = "application/json") }),
                        @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
                        @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
        @GetMapping("/prevision")
        public ResponseEntity<List<GenericOutputModel>> invokeExternalServicePrevision() {
                List<GenericOutputModel> result = externalServiceInvoker
                                .invokeIndisaPrevision();
                if (result.isEmpty())
                        new ResponseEntity<>(HttpStatus.NOT_FOUND);
                return new ResponseEntity<>(result, HttpStatus.OK);
        }

        @Operation(summary = "lista de especialidades", description = "traer todas las especialidades de la clínica Indisa por sucursal y previsión")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", content = {
                                        @Content(schema = @Schema(implementation = List.class), mediaType = "application/json") }),
                        @ApiResponse(responseCode = "400", content = { @Content(schema = @Schema()) }),
                        @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
                        @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
        @GetMapping("/speciality")
        public ResponseEntity<List<GenericOutputModel>> invokeExternalServiceSpeciality(
                        @Parameter(description = "ID de la previsión", example = "67", required = true) @RequestParam String codePrevision,
                        @Parameter(description = "Nombre de la sucursal", example = "PROVIDENCIA", required = true) @RequestParam String office) {
                if (office == null || codePrevision == null)
                        new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                List<GenericOutputModel> result = externalServiceInvoker
                                .invokeIndisaSpeciality(codePrevision, office);
                if (result.isEmpty())
                        new ResponseEntity<>(HttpStatus.NOT_FOUND);
                return new ResponseEntity<>(result, HttpStatus.OK);
        }

        @Operation(summary = "lista de los médicos", description = "traer los médicos de la clínica Indisa por sucursal, previsión y especialidad")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", content = {
                                        @Content(schema = @Schema(implementation = List.class), mediaType = "application/json") }),
                        @ApiResponse(responseCode = "400", content = { @Content(schema = @Schema()) }),
                        @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
                        @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
        @GetMapping("/doctor")
        public ResponseEntity<MedicalAgreementModel> invokeExternalServiceDoctors(
                        @Parameter(description = "ID de la previsión", example = "67", required = true) @RequestParam String codePrevision,
                        @Parameter(description = "Nombre de la sucursal", example = "PROVIDENCIA", required = true) @RequestParam String office,
                        @Parameter(description = "ID de la especialidad", example = "1", required = true) @RequestParam String codeSpeciality) {
                if (office == null || codePrevision == null || codeSpeciality == null)
                        new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                MedicalAgreementModel result = externalServiceInvoker
                                .invokeIndisaDoctors(codePrevision, office, codeSpeciality);
                if (result.whith().isEmpty() && result.whithout().isEmpty())
                        new ResponseEntity<>(HttpStatus.NOT_FOUND);
                return new ResponseEntity<>(result, HttpStatus.OK);
        }

}
