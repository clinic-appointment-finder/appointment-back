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
    public ResponseEntity<List<TaskProgramOutputModel>> findAllTaskProgram(
            @Parameter(description = "Paginación -> Número de página", example = "0", required = true) @RequestParam Integer page,
            @Parameter(description = "Paginación -> cantidad de registros por página", example = "5", required = true) @RequestParam Integer size,
            @Parameter(description = "es una tarea válida, cuando la fecha actual esta entre la fecha desde y fecha hasta", example = "true", required =  false) @RequestParam(required = false) Boolean isTaskValidate,
            @Parameter(description = "hay un flag en BD que indica si es una tarea activa", example = "true", allowEmptyValue = true) @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Sucursal de la clínica", example = "MAIPU", allowEmptyValue = true) @RequestParam(required = false) String office) {
        if (page == null || size == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Page<TaskProgram> pageTask = taskProgramService.FindAll(page, size, isTaskValidate, isActive, office);
        return new ResponseEntity<>(transformEntityToOutput(pageTask.getContent()), HttpStatus.OK);
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<?> getTaskProgramById(@PathVariable Long id) {
        if (id == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<TaskProgram> taskProgram = taskProgramService.FindByID(id);

        if (taskProgram.isPresent()) {
            List<TaskProgramOutputModel> resultList = transformEntityToOutput(Collections.singletonList(taskProgram.get()));
            return new ResponseEntity<>(resultList.isEmpty() ? "{}" : resultList.get(0), HttpStatus.OK);
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
    public ResponseEntity<HttpStatus> updateTaskProgramActive(@PathVariable Long id, @PathVariable Integer doctorId,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean isNotify = requestBody.get("isNotify");
        int cantUpdate = appointmentDoctorService.updateAppointmentDoctorNotify(id, doctorId, isNotify);
        if (cantUpdate == 1) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private List<TaskProgramOutputModel> transformEntityToOutput(List<TaskProgram> programs) {
        List<TaskProgramOutputModel> returnList = new ArrayList<>();
        programs.forEach(taskProgram -> {
            List<GenericOutputModel> specialities = indisaServiceInvoker
                    .invokeIndisaSpeciality(taskProgram.getPrevisionId().toString(), taskProgram.getOfficeName());
            List<GenericOutputModel> previsionList = indisaServiceInvoker.invokeIndisaPrevision();
            List<DoctorAppointmentOutputModel> doctorsList = transformDoctors(taskProgram.getDoctors(),
                    indisaServiceInvoker.invokeIndisaDoctors(taskProgram.getPrevisionId().toString(),
                            taskProgram.getOfficeName(), taskProgram.getSpecialityId().toString()));
            GenericOutputModel speciality = findSpeciality(specialities, taskProgram.getSpecialityId().toString());
            GenericOutputModel prevision = findPrevision(previsionList, taskProgram.getPrevisionId().toString());

            returnList.add(new TaskProgramOutputModel(
                    taskProgram.getTaskProgramId(),
                    taskProgram.getClinic(),
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
        return returnList;
    }

    private GenericOutputModel findSpeciality(List<GenericOutputModel> specialities, String specialityId) {
        return findGenericOutputModelByCode(specialities, specialityId);
    }

    private GenericOutputModel findPrevision(List<GenericOutputModel> previsionList, String previsionId) {
        return findGenericOutputModelByCode(previsionList, previsionId);
    }

    private GenericOutputModel findGenericOutputModelByCode(List<GenericOutputModel> models, String code) {
        Optional<GenericOutputModel> matchingModel = models.stream()
                .filter(model -> model.code().equals(code))
                .findFirst();
        return matchingModel.orElse(null);
    }

    private List<DoctorAppointmentOutputModel> transformDoctors(List<AppointmentFound> doctors,
            MedicalAgreementModel medicalAgreementModel) {
        List<DoctorAppointmentOutputModel> doctorsList = new ArrayList<>();
        doctors.forEach(doctor -> {
            DoctorModel matchingDoctor = findMatchingDoctor(doctor.getDoctorId().toString(),
                    medicalAgreementModel.whith());
            if (matchingDoctor != null) {
                doctorsList.add(new DoctorAppointmentOutputModel(doctor.getAppointmentFoundId(), doctor.getDoctorId(),
                        doctor.isNotify(), doctor.getTaskProgram(), matchingDoctor.name(), matchingDoctor.urlImage(),
                        true));
            } else {
                matchingDoctor = findMatchingDoctor(doctor.getDoctorId().toString(), medicalAgreementModel.whithout());
                if (matchingDoctor != null) {
                    doctorsList
                            .add(new DoctorAppointmentOutputModel(doctor.getAppointmentFoundId(), doctor.getDoctorId(),
                                    doctor.isNotify(), doctor.getTaskProgram(), matchingDoctor.name(),
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
