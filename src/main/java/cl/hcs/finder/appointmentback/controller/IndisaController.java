package cl.hcs.finder.appointmentback.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.model.TaskProgram;
import cl.hcs.finder.appointmentback.service.AppointmentDoctorService;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import cl.hcs.finder.appointmentback.service.TaskProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clinic/indisa")
@Tag(name = "Indisa Controller", description = "Endpoints para administrar las citas de la clinica Indisa")
public class IndisaController {

    @Autowired
    private final IndisaServiceInvoker externalServiceInvoker;

    @Autowired
    private final TaskProgramService taskProgramService;

    @Autowired
    private final AppointmentDoctorService appointmentDoctorService;

    public IndisaController(IndisaServiceInvoker externalServiceInvoker, TaskProgramService taskProgramService,
            AppointmentDoctorService appointmentDoctorService) {
        this.externalServiceInvoker = externalServiceInvoker;
        this.taskProgramService = taskProgramService;
        this.appointmentDoctorService = appointmentDoctorService;
    }

    @Operation(summary = "Buscar cita en la clínica", description = "Busca una cita para un doctor con la API de la clínica Indisa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = IndisaCalendarOutputModel.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/external/appointments")
    public Mono<IndisaCalendarOutputModel> invokeExternalService(
            @Parameter(description = "identificador único de la sesión para usar api externa", example = "65af298ffc465ea2f7dedb49", required = true) @RequestParam String agendaID,
            @Parameter(description = "ID de la Especialidad médica", example = "226", required = true) @RequestParam String specialityID,
            @Parameter(description = "ID del doctor asociada a la sucursal ", example = "14655", required = true) @RequestParam String doctorID,
            @Parameter(description = "sucursal de la Clínica", example = "PROVIDENCIA", required = true) @RequestParam String office) {
        return externalServiceInvoker
                .invokeExternalIndisaCalendarEndpoint(
                        new IndisaCalendarInputModel(agendaID, specialityID, doctorID, office));
    }

    @Operation(summary = "Crea una tarea programada", description = "Crear un registro de búsqueda de cita para una tarea programada")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {
                    @Content(schema = @Schema(implementation = TaskProgram.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @PostMapping("/appointments")
    public ResponseEntity<TaskProgram> createTaskProgram(
            @Schema(description = "Input body para crear una tarea", example = "{\"previsionID\": 67,\n" + //
                    "  \"startDate\": \"2024-01-15\",\n" + //
                    "  \"endDate\": \"2024-01-20\",\n" + //
                    "  \"specialityID\": 226,\n" + //
                    "  \"doctorsIDs\": [14655, 12375],\n" + //
                    "  \"office\": \"PROVIDENCIA\",\n" + //
                    "  \"emails\": [\"email1@example.com\", \"email2@example.com\"]}", required = true) @RequestBody IndisaAppointmentInputModel inputModel) {

        TaskProgram createdTaskProgram = taskProgramService.createTaskProgram(inputModel);
        return new ResponseEntity<>(createdTaskProgram, HttpStatus.CREATED);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<TaskProgram>> findAllTaskProgram() {
        List<TaskProgram> list = taskProgramService.FindAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<TaskProgram> getTaskProgramById(@PathVariable Long id) {
        if (id == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<TaskProgram> taskProgram = taskProgramService.FindByID(id);

        if (taskProgram.isPresent()) {
            return new ResponseEntity<>(taskProgram.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    @PatchMapping("/appointments/{id}")
    public ResponseEntity<TaskProgram> updateTaskProgramActive(@PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean isActive = requestBody.get("isActive");
        int cantUpdate = taskProgramService.updateTaskProgramActive(id, isActive);

        if (cantUpdate == 1) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/appointments/{id}/doctors/{doctorId}")
    public ResponseEntity<TaskProgram> updateTaskProgramActive(@PathVariable Long id, @PathVariable Integer doctorId,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean isNotify = requestBody.get("isNotify");
        int cantUpdate = appointmentDoctorService.updateAppointmentDoctorNotify(id, doctorId, isNotify);
        if (cantUpdate == 1) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
