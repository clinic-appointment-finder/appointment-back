package cl.hcs.finder.appointmentback.controller;

import java.util.ArrayList;
import java.util.Collections;
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

import cl.hcs.finder.appointmentback.entity.AppointmentFound;
import cl.hcs.finder.appointmentback.entity.Clinic;
import cl.hcs.finder.appointmentback.entity.TaskProgram;
import cl.hcs.finder.appointmentback.model.DoctorAppointmentOutputModel;
import cl.hcs.finder.appointmentback.model.GenericOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel;
import cl.hcs.finder.appointmentback.model.TaskProgramOutputModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel.DoctorModel;
import cl.hcs.finder.appointmentback.service.AppointmentDoctorService;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import cl.hcs.finder.appointmentback.service.TaskProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clinic/indisa")
@Tag(name = "Indisa Controller", description = "Endpoints para administrar las citas de la clinica Indisa")
public class IndisaController {

    @Autowired
    private final TaskProgramService taskProgramService;

    @Autowired
    private final IndisaServiceInvoker indisaServiceInvoker;

    @Autowired
    private final AppointmentDoctorService appointmentDoctorService;

    public IndisaController(AppointmentDoctorService appointmentDoctorService, TaskProgramService taskProgramService,
            IndisaServiceInvoker indisaServiceInvoker) {
        this.taskProgramService = taskProgramService;
        this.appointmentDoctorService = appointmentDoctorService;
        this.indisaServiceInvoker = indisaServiceInvoker;
    }

    @Operation(summary = "Crea una tarea programada", description = "Crear un registro de búsqueda de cita para una tarea programada")
    @ApiResponses({
            @ApiResponse(responseCode = "201", content = {
                    @Content(schema = @Schema(implementation = TaskProgram.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @PostMapping("/appointments")
    public Mono<ResponseEntity<TaskProgram>> createTaskProgram(
            @Schema(description = "Input body para crear una tarea", example = "{\"prevision_id\": 67,\n" + //
                    "  \"start_date\": \"2024-01-15\",\n" + //
                    "  \"end_date\": \"2024-01-20\",\n" + //
                    "  \"speciality_id\": 226,\n" + //
                    "  \"doctors_ids\": [14655, 12375],\n" + //
                    "  \"office\": \"PROVIDENCIA\",\n" + //
                    "  \"emails\": [\"email1@example.com\", \"email2@example.com\"]}", required = true) @RequestBody IndisaAppointmentInputModel inputModel) {
        return taskProgramService.createTaskProgram(inputModel)
                .map(createdTaskProgram -> ResponseEntity.status(HttpStatus.CREATED).body(createdTaskProgram));
    }

    @Operation(summary = "Buscar los programas", description = "trae todos las tareas programadas existentes en la base de datos con paginación")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = List.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/appointments")
    public Mono<ResponseEntity<List<TaskProgramOutputModel>>> findAllTaskProgram(
            @Parameter(description = "Paginación -> Número de página", example = "0", required = true) @RequestParam Integer page,
            @Parameter(description = "Paginación -> cantidad de registros por página", example = "5", required = true) @RequestParam Integer size,
            @Parameter(description = "es una tarea válida, cuando la fecha actual esta entre la fecha desde y fecha hasta", example = "true", required = false) @RequestParam(required = false) Boolean isTaskValidate,
            @Parameter(description = "hay un flag en BD que indica si es una tarea activa", example = "true", allowEmptyValue = true) @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Sucursal de la clínica", example = "MAIPU", allowEmptyValue = true) @RequestParam(required = false) String office,
            @Parameter(description = "TRUE para ofuscar el correo (default) y FALSE para mostrar correo completo", example = "true", allowEmptyValue = true) @RequestParam(required = false, defaultValue = "true") Boolean obfuscateMail) {
        if (page == null || size == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        return taskProgramService.FindAll(page, size, isTaskValidate, isActive, office, obfuscateMail)
                .flatMap(pageTask -> {
                    return transformEntityToOutput(pageTask.getContent())
                            .map(ResponseEntity::ok)
                            .defaultIfEmpty(ResponseEntity.notFound().build());
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @Operation(summary = "Buscar la tarea programada por ID", description = "Buscar la tarea programada por ID único de la base de datos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = {
                    @Content(schema = @Schema(implementation = TaskProgramOutputModel.class), mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "404", content = { @Content(schema = @Schema()) }),
            @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
    @GetMapping("/appointments/{id}")
    public Mono<ResponseEntity<TaskProgramOutputModel>> getTaskProgramById(@PathVariable Long id) {
        if (id == null)
            return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        return taskProgramService.FindByID(id)
                .flatMap(taskProgram -> {
                    return transformEntityToOutput(Collections.singletonList(taskProgram))
                            .map(outputModel -> ResponseEntity.ok(outputModel.get(0)))
                            .defaultIfEmpty(ResponseEntity.notFound().build());
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @PatchMapping("/appointments/{id}")
    public Mono<ResponseEntity<Void>> updateTaskProgramActive(@PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean isActive = requestBody.getOrDefault("isActive", false);
        return taskProgramService.updateTaskProgramActive(id, isActive)
                .map(cantUpdate -> {
                    if (cantUpdate == 1) {
                        return ResponseEntity.ok().build();
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

    @PatchMapping("/appointments/{id}/doctors/{doctorId}")
    public Mono<ResponseEntity<HttpStatus>> updateDoctorNotify(@PathVariable Long id, @PathVariable Integer doctorId,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean isNotify = requestBody.getOrDefault("isNotify", false);
        return appointmentDoctorService.updateAppointmentDoctorNotify(id, doctorId, isNotify)
                .map(cantUpdate -> {
                    if (cantUpdate == 1) {
                        return ResponseEntity.ok().build();
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

    private Mono<List<TaskProgramOutputModel>> transformEntityToOutput(List<TaskProgram> programs) {
        return Flux.fromIterable(programs)
                .flatMap(taskProgram -> {
                    return indisaServiceInvoker.invokeIndisaSpeciality(taskProgram.getPrevisionId().toString(),
                            taskProgram.getOfficeName())
                            .flatMapMany(specialities -> {
                                Mono<List<GenericOutputModel>> previsionListMono = indisaServiceInvoker
                                        .invokeIndisaPrevision().collectList();
                                Mono<MedicalAgreementModel> doctorsMono = indisaServiceInvoker.invokeIndisaDoctors(
                                        taskProgram.getPrevisionId().toString(),
                                        taskProgram.getOfficeName(),
                                        taskProgram.getSpecialityId().toString());

                                return Mono.zip(previsionListMono, doctorsMono)
                                        .flatMapMany(tuple -> {
                                            List<GenericOutputModel> previsionList = tuple.getT1();
                                            MedicalAgreementModel doctors = tuple.getT2();
                                            GenericOutputModel speciality = findSpeciality(specialities,
                                                    taskProgram.getSpecialityId().toString());
                                            GenericOutputModel prevision = findPrevision(previsionList,
                                                    taskProgram.getPrevisionId().toString());
                                            List<DoctorAppointmentOutputModel> doctorsList = transformDoctors(
                                                    taskProgram.getDoctors(), doctors);

                                            return Flux.just(new TaskProgramOutputModel(
                                                    taskProgram.getTaskProgramId(),
                                                    new Clinic(1, "Clínica Indisa"),
                                                    doctorsList,
                                                    prevision,
                                                    taskProgram.getStartDate(),
                                                    taskProgram.getEndDate(),
                                                    taskProgram.getOfficeName(),
                                                    speciality,
                                                    taskProgram.getEmails(),
                                                    taskProgram.getCreationDate(),
                                                    taskProgram.isActive()));
                                        });
                            });
                })
                .collectList();
    }

    private GenericOutputModel findSpeciality(List<GenericOutputModel> specialities, String specialityId) {
        Optional<GenericOutputModel> matchingSpeciality = specialities.stream()
                .filter(speciality -> speciality.code().equals(specialityId))
                .findFirst();
        return matchingSpeciality.orElse(new GenericOutputModel(specialityId, "")); // Retorna un objeto con solo el
                                                                                    // código si no se encuentra el
                                                                                    // nombre
    }

    private GenericOutputModel findPrevision(List<GenericOutputModel> previsionList, String previsionId) {
        Optional<GenericOutputModel> matchingPrevision = previsionList.stream()
                .filter(prevision -> prevision.code().equals(previsionId))
                .findFirst();
        return matchingPrevision.orElse(new GenericOutputModel(previsionId, "")); // Retorna un objeto con solo el
                                                                                  // código si no se encuentra el nombre
    }

    private List<DoctorAppointmentOutputModel> transformDoctors(List<AppointmentFound> doctors,
            MedicalAgreementModel medicalAgreementModel) {
        List<DoctorAppointmentOutputModel> doctorsList = new ArrayList<>();
        doctors.forEach(doctor -> {
            DoctorModel matchingDoctor = findMatchingDoctor(doctor.getDoctorId().toString(),
                    medicalAgreementModel.whith());
            if (matchingDoctor != null) {
                doctorsList.add(new DoctorAppointmentOutputModel(doctor.getAppointmentFoundId(), doctor.getDoctorId(),
                        doctor.isNotify(), matchingDoctor.name(), matchingDoctor.urlImage(),
                        true));
            } else {
                matchingDoctor = findMatchingDoctor(doctor.getDoctorId().toString(), medicalAgreementModel.whithout());
                if (matchingDoctor != null) {
                    doctorsList
                            .add(new DoctorAppointmentOutputModel(doctor.getAppointmentFoundId(), doctor.getDoctorId(),
                                    doctor.isNotify(), matchingDoctor.name(),
                                    matchingDoctor.urlImage(), false));
                }
            }

        });
        return doctorsList;
    }

    private DoctorModel findMatchingDoctor(String doctorId, List<DoctorModel> docsModel) {
        Optional<DoctorModel> matchingDoctor = docsModel.stream()
                .filter(whith -> doctorId.equals(whith.code()))
                .findFirst();
        return matchingDoctor.orElse(null);
    }
}
