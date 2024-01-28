package cl.hcs.finder.appointmentback.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import cl.hcs.finder.appointmentback.entity.TaskProgram;
import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.service.AppointmentDoctorService;

import cl.hcs.finder.appointmentback.service.TaskProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/clinic/indisa")
@Tag(name = "Indisa Controller", description = "Endpoints para administrar las citas de la clinica Indisa")
public class IndisaController {    

    @Autowired
    private final TaskProgramService taskProgramService;

    @Autowired
    private final AppointmentDoctorService appointmentDoctorService;

    public IndisaController(AppointmentDoctorService appointmentDoctorService, TaskProgramService taskProgramService) {        
        this.taskProgramService = taskProgramService;
        this.appointmentDoctorService = appointmentDoctorService;
    }    

    @Operation(summary = "Crea una tarea programada", description = "Crear un registro de búsqueda de cita para una tarea programada")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {
                    @Content(schema = @Schema(implementation = TaskProgram.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @PostMapping("/appointments")
    public ResponseEntity<TaskProgram> createTaskProgram(
            @Schema(description = "Input body para crear una tarea", example = "{\"prevision_id\": 67,\n" + //
                    "  \"start_date\": \"2024-01-15\",\n" + //
                    "  \"end_date\": \"2024-01-20\",\n" + //
                    "  \"speciality_id\": 226,\n" + //
                    "  \"doctors_ids\": [14655, 12375],\n" + //
                    "  \"office\": \"PROVIDENCIA\",\n" + //
                    "  \"emails\": [\"email1@example.com\", \"email2@example.com\"]}", required = true) @RequestBody IndisaAppointmentInputModel inputModel) {

        TaskProgram createdTaskProgram = taskProgramService.createTaskProgram(inputModel);
        return new ResponseEntity<>(createdTaskProgram, HttpStatus.CREATED);
    }

    @Operation(summary = "Buscar los programas", description = "trae todos las tareas programadas existentes en la base de datos con paginación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = List.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/appointments")
    public ResponseEntity<List<TaskProgram>> findAllTaskProgram(
            @Parameter(description = "Paginación -> Número de página", example = "0", required = true) @RequestParam Integer page,
            @Parameter(description = "Paginación -> cantidad de registros por página", example = "5", required = true) @RequestParam Integer size,
            @Parameter(description = "es una tarea válida, cuando la fecha actual esta entre la fecha desde y fecha hasta", example = "true", allowEmptyValue = true) @RequestParam Boolean isTaskValidate,
            @Parameter(description = "hay un flag en BD que indica si es una tarea activa", example = "true", allowEmptyValue = true) @RequestParam Boolean isActive,
            @Parameter(description = "Sucursal de la clínica", example = "MAIPU", allowEmptyValue = true ) @RequestParam String office) {
        if (page == null || size == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Page<TaskProgram> pageTask = taskProgramService.FindAll(page, size, isTaskValidate, isActive, office);
        return new ResponseEntity<>(pageTask.getContent(), HttpStatus.OK);
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
