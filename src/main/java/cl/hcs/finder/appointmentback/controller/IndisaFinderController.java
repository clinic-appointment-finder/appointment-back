package cl.hcs.finder.appointmentback.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.hcs.finder.appointmentback.entity.AppointmentFound;
import cl.hcs.finder.appointmentback.entity.TaskProgram;
import cl.hcs.finder.appointmentback.model.FinderAppointmentOutputModel;
import cl.hcs.finder.appointmentback.model.FinderTaskAppointmentOutputModel;
import cl.hcs.finder.appointmentback.model.FinderCandidateAppointmentInputModel;
import cl.hcs.finder.appointmentback.model.FinderDoctorAppointmentsOutputModel;
import cl.hcs.finder.appointmentback.model.GenericOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaProposeHoursInputModel;
import cl.hcs.finder.appointmentback.model.IndisaProposeHoursOutputModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel.DoctorModel;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import cl.hcs.finder.appointmentback.service.TaskProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clinic/indisa/finder")
@Tag(name = "Indisa Finder Controller", description = "Busca horas disponibles para tareas vigentes y activas")
public class IndisaFinderController {

    private static final Logger log = LoggerFactory.getLogger(IndisaFinderController.class);
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final List<DateTimeFormatter> INDISA_DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE);

    private final TaskProgramService taskProgramService;
    private final IndisaServiceInvoker indisaServiceInvoker;

    public IndisaFinderController(TaskProgramService taskProgramService,
            IndisaServiceInvoker indisaServiceInvoker) {
        this.taskProgramService = taskProgramService;
        this.indisaServiceInvoker = indisaServiceInvoker;
    }

    @Operation(summary = "Buscar horas disponibles",
            description = "Obtiene las tareas vigentes y activas, consulta el calendario y los horarios de Indisa para cada médico y fecha. Enriquece la respuesta con datos de la clínica, prevision, especialidad, médicos (nombre, imagen, convenio) y correos de notificación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FinderTaskAppointmentOutputModel.class)), mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error consultando Indisa") })
    @PostMapping("/appointments")
    public Mono<ResponseEntity<List<FinderTaskAppointmentOutputModel>>> findAppointments() {
        ResponseEntity<List<FinderTaskAppointmentOutputModel>> internalServerError =
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        return taskProgramService.FindAll(0, Integer.MAX_VALUE, true, true, null, false)
                .flatMapMany(page -> Flux.fromIterable(page.getContent()))
                .flatMap(this::enrichTaskAppointments)
                .collectList()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("Error buscando horas disponibles de Indisa", error);
                    return Mono.<ResponseEntity<List<FinderTaskAppointmentOutputModel>>>just(internalServerError);
                });
    }

    @Operation(summary = "Buscar citas candidatas",
            description = "Consulta disponibilidad de Indisa sin crear ni leer una tarea desde la base de datos. Enriquece la respuesta con datos del médico (nombre, imagen, convenio) obtenidos desde Indisa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FinderDoctorAppointmentsOutputModel.class)), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error consultando Indisa") })
    @PostMapping("/appointments/candidates")
    public Mono<ResponseEntity<List<FinderDoctorAppointmentsOutputModel>>> findCandidateAppointments(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Criterios de búsqueda. doctors_ids contiene uno o varios médicos.", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = FinderCandidateAppointmentInputModel.class), examples = @ExampleObject(value = "{\n  \"prevision_id\": 67,\n  \"start_date\": \"2026-08-04\",\n  \"end_date\": \"2026-08-09\",\n  \"start_time\": \"09:00:00\",\n  \"end_time\": \"18:00:00\",\n  \"speciality_id\": 226,\n  \"doctors_ids\": [18379, 17982],\n  \"office\": \"PROVIDENCIA\"\n}"))) @RequestBody FinderCandidateAppointmentInputModel input) {
        try {
            TaskProgram candidateTask = buildCandidateTask(input);
            ResponseEntity<List<FinderDoctorAppointmentsOutputModel>> internalServerError =
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

            return enrichCandidateAppointments(candidateTask)
                    .map(ResponseEntity::ok)
                    .onErrorResume(error -> {
                        log.error("Error buscando citas candidatas de Indisa", error);
                        return Mono.<ResponseEntity<List<FinderDoctorAppointmentsOutputModel>>>just(internalServerError);
                    });
        } catch (RuntimeException error) {
            log.warn("Parámetros inválidos para búsqueda de citas candidatas", error);
            return Mono.just(ResponseEntity.badRequest().build());
        }
    }

    private Mono<FinderTaskAppointmentOutputModel> enrichTaskAppointments(TaskProgram taskProgram) {
        return findTaskAppointments(taskProgram)
                .collectList()
                .map(appointments -> groupAppointmentsByDoctorWithTask(taskProgram, appointments))
                .filter(appointments -> !appointments.isEmpty())
                .flatMap(appointments -> enrichTaskDoctors(taskProgram, appointments)
                        .flatMap(enriched -> enrichTaskMetadata(taskProgram, enriched)));
    }

    private List<FinderDoctorAppointmentsOutputModel> groupAppointmentsByDoctorWithTask(
            TaskProgram taskProgram, List<FinderAppointmentOutputModel> appointments) {
        Map<Integer, AppointmentFound> doctorMap = taskProgram.getDoctors().stream()
                .collect(Collectors.toMap(AppointmentFound::getDoctorId, d -> d, (a, b) -> a, LinkedHashMap::new));

        Map<Integer, List<FinderAppointmentOutputModel>> grouped = appointments.stream()
                .sorted(Comparator.comparing(FinderAppointmentOutputModel::date)
                        .thenComparing(FinderAppointmentOutputModel::doctorId))
                .collect(Collectors.groupingBy(
                        FinderAppointmentOutputModel::doctorId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    AppointmentFound doctor = doctorMap.get(entry.getKey());
                    return new FinderDoctorAppointmentsOutputModel(
                            doctor != null ? doctor.getAppointmentFoundId() : null,
                            entry.getKey(),
                            doctor != null && doctor.isNotify(),
                            null,
                            null,
                            false,
                            entry.getValue());
                })
                .toList();
    }

    private Mono<List<FinderDoctorAppointmentsOutputModel>> enrichTaskDoctors(TaskProgram taskProgram,
            List<FinderDoctorAppointmentsOutputModel> appointments) {
        return indisaServiceInvoker.invokeIndisaDoctors(
                        taskProgram.getPrevisionId().toString(),
                        taskProgram.getOfficeName(),
                        taskProgram.getSpecialityId().toString())
                .map(agreement -> {
                    List<FinderDoctorAppointmentsOutputModel> enriched = new ArrayList<>();
                    for (FinderDoctorAppointmentsOutputModel doc : appointments) {
                        DoctorModel matching = findMatchingDoctor(
                                doc.doctorId().toString(), agreement.whith());
                        boolean haveAgreement = true;
                        String name = "unknown-" + doc.doctorId();
                        String urlImage = "";
                        if (matching == null) {
                            matching = findMatchingDoctor(
                                    doc.doctorId().toString(), agreement.whithout());
                            haveAgreement = false;
                        }
                        if (matching != null) {
                            name = matching.name();
                            urlImage = matching.urlImage();
                        }
                        enriched.add(new FinderDoctorAppointmentsOutputModel(
                                doc.appointmentFoundId(),
                                doc.doctorId(),
                                doc.isNotify(),
                                name,
                                urlImage,
                                haveAgreement,
                                doc.appointments()));
                    }
                    return enriched;
                })
                .onErrorResume(error -> {
                    log.warn("Error enriqueciendo doctores de tarea {}", taskProgram.getTaskProgramId(), error);
                    return Mono.just(appointments);
                });
    }

    private Mono<FinderTaskAppointmentOutputModel> enrichTaskMetadata(TaskProgram taskProgram,
            List<FinderDoctorAppointmentsOutputModel> appointments) {
        Mono<List<GenericOutputModel>> previsionListMono = indisaServiceInvoker
                .invokeIndisaPrevision().collectList();
        Mono<List<GenericOutputModel>> specialityMono = indisaServiceInvoker
                .invokeIndisaSpeciality(taskProgram.getPrevisionId().toString(),
                        taskProgram.getOfficeName());

        return Mono.zip(previsionListMono, specialityMono)
                .map(tuple -> {
                    GenericOutputModel prevision = findGeneric(tuple.getT1(),
                            taskProgram.getPrevisionId().toString());
                    GenericOutputModel speciality = findGeneric(tuple.getT2(),
                            taskProgram.getSpecialityId().toString());
                    return new FinderTaskAppointmentOutputModel(
                            taskProgram.getTaskProgramId(),
                            taskProgram.getClinic() != null ? taskProgram.getClinic().getName() : null,
                            taskProgram.getEmails(),
                            prevision,
                            speciality,
                            taskProgram.getOfficeName(),
                            taskProgram.getStartDate(),
                            taskProgram.getEndDate(),
                            taskProgram.getStartTime(),
                            taskProgram.getEndTime(),
                            taskProgram.getCreationDate(),
                            taskProgram.isActive(),
                            appointments);
                })
                .onErrorResume(error -> {
                    log.warn("Error enriqueciendo metadatos de tarea {}", taskProgram.getTaskProgramId(), error);
                    return Mono.just(new FinderTaskAppointmentOutputModel(
                            taskProgram.getTaskProgramId(),
                            taskProgram.getClinic() != null ? taskProgram.getClinic().getName() : null,
                            taskProgram.getEmails(),
                            new GenericOutputModel(taskProgram.getPrevisionId().toString(), ""),
                            new GenericOutputModel(taskProgram.getSpecialityId().toString(), ""),
                            taskProgram.getOfficeName(),
                            taskProgram.getStartDate(),
                            taskProgram.getEndDate(),
                            taskProgram.getStartTime(),
                            taskProgram.getEndTime(),
                            taskProgram.getCreationDate(),
                            taskProgram.isActive(),
                            appointments));
                });
    }

    private Mono<List<FinderDoctorAppointmentsOutputModel>> enrichCandidateAppointments(TaskProgram candidateTask) {
        return findTaskAppointments(candidateTask)
                .collectList()
                .map(this::groupAppointmentsByDoctor)
                .flatMap(appointments -> enrichCandidateDoctors(candidateTask, appointments));
    }

    private Mono<List<FinderDoctorAppointmentsOutputModel>> enrichCandidateDoctors(TaskProgram taskProgram,
            List<FinderDoctorAppointmentsOutputModel> appointments) {
        return indisaServiceInvoker.invokeIndisaDoctors(
                        taskProgram.getPrevisionId().toString(),
                        taskProgram.getOfficeName(),
                        taskProgram.getSpecialityId().toString())
                .map(agreement -> {
                    Map<Integer, FinderDoctorAppointmentsOutputModel> enriched = new LinkedHashMap<>();
                    for (FinderDoctorAppointmentsOutputModel doc : appointments) {
                        DoctorModel matching = findMatchingDoctor(
                                doc.doctorId().toString(), agreement.whith());
                        boolean haveAgreement = true;
                        String name = "unknown-" + doc.doctorId();
                        String urlImage = "";
                        if (matching == null) {
                            matching = findMatchingDoctor(
                                    doc.doctorId().toString(), agreement.whithout());
                            haveAgreement = false;
                        }
                        if (matching != null) {
                            name = matching.name();
                            urlImage = matching.urlImage();
                        }
                        enriched.put(doc.doctorId(), new FinderDoctorAppointmentsOutputModel(
                                doc.appointmentFoundId(),
                                doc.doctorId(),
                                doc.isNotify(),
                                name,
                                urlImage,
                                haveAgreement,
                                doc.appointments()));
                    }
                    List<FinderDoctorAppointmentsOutputModel> result = new ArrayList<>(enriched.values());
                    return result;
                })
                .onErrorResume(error -> {
                    log.warn("Error enriqueciendo doctores candidatos", error);
                    return Mono.just(new ArrayList<>(appointments));
                });
    }

    private List<FinderDoctorAppointmentsOutputModel> groupAppointmentsByDoctor(
            List<FinderAppointmentOutputModel> appointments) {
        Map<Integer, List<FinderAppointmentOutputModel>> grouped = appointments.stream()
                .sorted(Comparator.comparing(FinderAppointmentOutputModel::date)
                        .thenComparing(FinderAppointmentOutputModel::doctorId))
                .collect(Collectors.groupingBy(
                        FinderAppointmentOutputModel::doctorId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(entry -> new FinderDoctorAppointmentsOutputModel(
                        null,
                        entry.getKey(),
                        false,
                        null,
                        null,
                        false,
                        entry.getValue()))
                .toList();
    }

    private TaskProgram buildCandidateTask(FinderCandidateAppointmentInputModel input) {
        if (input.previsionId() == null || input.specialityId() == null || input.office() == null
                || input.office().isBlank() || input.startDate() == null || input.endDate() == null) {
            throw new IllegalArgumentException("Faltan parámetros obligatorios de búsqueda");
        }

        TaskProgram task = new TaskProgram();
        task.setPrevisionId(input.previsionId());
        task.setSpecialityId(input.specialityId());
        task.setOfficeName(input.office());
        task.setStartDate(LocalDate.parse(input.startDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        task.setEndDate(LocalDate.parse(input.endDate(), DateTimeFormatter.ISO_LOCAL_DATE));
        task.setStartTime(input.startTime());
        task.setEndTime(input.endTime());

        Set<Integer> doctorIds = new LinkedHashSet<>();
        if (input.doctorsIds() != null) {
            doctorIds.addAll(Arrays.asList(input.doctorsIds()));
        }
        if (doctorIds.isEmpty() || task.getEndDate().isBefore(task.getStartDate())) {
            throw new IllegalArgumentException("El rango de fechas y los médicos son obligatorios");
        }

        List<AppointmentFound> doctors = new ArrayList<>();
        doctorIds.forEach(doctorId -> {
            AppointmentFound doctor = new AppointmentFound();
            doctor.setDoctorId(doctorId);
            doctors.add(doctor);
        });
        task.setDoctors(doctors);
        return task;
    }

    private Flux<FinderAppointmentOutputModel> findTaskAppointments(TaskProgram taskProgram) {
        return Flux.fromIterable(taskProgram.getDoctors())
                .flatMap(doctor -> findDoctorAppointments(taskProgram, doctor));
    }

    private Flux<FinderAppointmentOutputModel> findDoctorAppointments(TaskProgram taskProgram,
            AppointmentFound doctor) {
        LocalDate effectiveStart = taskProgram.getStartDate();
        if (effectiveStart.isBefore(LocalDate.now())) {
            effectiveStart = LocalDate.now();
        }
        final LocalDate searchStart = effectiveStart;

        IndisaCalendarInputModel calendarInput = new IndisaCalendarInputModel(
                taskProgram.getSpecialityId().toString(),
                doctor.getDoctorId().toString(),
                taskProgram.getOfficeName(),
                taskProgram.getPrevisionId().toString());

        Flux<IndisaCalendarOutputModel> generalCalendar = indisaServiceInvoker
                .invokeIndisaCalendar(calendarInput)
                .flux()
                .doOnError(error -> log.error(
                        "Error consultando calendario general de Indisa. taskProgramId={}, doctorId={}",
                        taskProgram.getTaskProgramId(), doctor.getDoctorId(), error));

        Flux<String> monthlyAvailableDates = Flux
                .fromIterable(monthsBetween(searchStart, taskProgram.getEndDate()))
                .flatMap(month -> {
                    LocalDate monthStart = month.atDay(1).isAfter(searchStart)
                            ? month.atDay(1) : searchStart;
                    LocalDate monthEnd = month.atEndOfMonth().isBefore(taskProgram.getEndDate())
                            ? month.atEndOfMonth() : taskProgram.getEndDate();
                    return indisaServiceInvoker.invokeIndisaCalendar(calendarInput,
                                    month.getMonthValue(), month.getYear())
                        .doOnError(error -> log.error(
                                "Error consultando calendario mensual de Indisa. taskProgramId={}, doctorId={}, month={}",
                                taskProgram.getTaskProgramId(), doctor.getDoctorId(), month, error))
                        .flatMapMany(calendar -> Flux.fromIterable(
                                extractAvailableDates(calendar, monthStart, monthEnd)));
                });

        Flux<String> generalAvailableDates = generalCalendar
                .flatMap(calendar -> Flux.fromIterable(extractAvailableDates(
                        calendar, searchStart, taskProgram.getEndDate())));

        return Flux.concat(generalAvailableDates, monthlyAvailableDates)
                .distinct()
                .flatMap(date -> findDateAppointments(taskProgram, doctor, date));
    }

    private List<YearMonth> monthsBetween(LocalDate startDate, LocalDate endDate) {
        List<YearMonth> months = new java.util.ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth last = YearMonth.from(endDate);
        while (!current.isAfter(last)) {
            months.add(current);
            current = current.plusMonths(1);
        }
        return months;
    }

    static List<String> extractAvailableDates(
            cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel calendar,
            LocalDate startDate, LocalDate endDate) {
        Set<LocalDate> unavailableDates = calendar.data() == null
                ? Set.of()
                : Arrays.stream(calendar.data())
                        .map(IndisaFinderController::parseUnavailableDate)
                        .collect(java.util.stream.Collectors.toSet());

        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> !unavailableDates.contains(date))
                .sorted()
                .map(LocalDate::toString)
                .toList();
    }

    private Mono<FinderAppointmentOutputModel> findDateAppointments(TaskProgram taskProgram,
            AppointmentFound doctor, String rawDate) {
        LocalDate date = parseIndisaDate(rawDate);
        if (date.isBefore(taskProgram.getStartDate()) || date.isAfter(taskProgram.getEndDate())) {
            return Mono.empty();
        }

        IndisaProposeHoursInputModel hoursInput = new IndisaProposeHoursInputModel(
                taskProgram.getSpecialityId().toString(),
                doctor.getDoctorId().toString(),
                taskProgram.getOfficeName(),
                taskProgram.getPrevisionId().toString(),
                date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));

        return indisaServiceInvoker.invokeIndisaProposeHours(hoursInput)
                .doOnError(error -> log.error(
                        "Error consultando horarios de Indisa. taskProgramId={}, doctorId={}, date={}",
                        taskProgram.getTaskProgramId(), doctor.getDoctorId(), rawDate, error))
                .map(hours -> new FinderAppointmentOutputModel(
                        doctor.getDoctorId(),
                        date.format(OUTPUT_DATE_FORMAT),
                        filterHours(hours, taskProgram.getStartTime(), taskProgram.getEndTime())))
                .filter(result -> !result.hours().isEmpty());
    }

    private List<String> filterHours(IndisaProposeHoursOutputModel availableHours,
            LocalTime startTime, LocalTime endTime) {
        return availableHours.hours().stream()
                .filter(hour -> isWithinConfiguredRange(hour, startTime, endTime))
                .toList();
    }

    static boolean isWithinConfiguredRange(String hour, LocalTime startTime, LocalTime endTime) {
        if (startTime == null && endTime == null) {
            return true;
        }

        LocalTime availableTime = LocalTime.parse(hour, DateTimeFormatter.ofPattern("H:mm"));
        return (startTime == null || !availableTime.isBefore(startTime))
                && (endTime == null || !availableTime.isAfter(endTime));
    }

    private GenericOutputModel findGeneric(List<GenericOutputModel> list, String code) {
        Optional<GenericOutputModel> match = list.stream()
                .filter(item -> item.code().equals(code))
                .findFirst();
        return match.orElse(new GenericOutputModel(code, ""));
    }

    private DoctorModel findMatchingDoctor(String doctorId, List<DoctorModel> docsModel) {
        Optional<DoctorModel> matchingDoctor = docsModel.stream()
                .filter(whith -> doctorId.equals(whith.code()))
                .findFirst();
        return matchingDoctor.orElse(null);
    }

    private static LocalDate parseIndisaDate(String rawDate) {
        return INDISA_DATE_FORMATS.stream()
                .flatMap(formatter -> parseDate(rawDate, formatter))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Fecha de Indisa no reconocida: " + rawDate));
    }

    private static LocalDate parseUnavailableDate(String rawDate) {
        return LocalDate.parse(rawDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private static Stream<LocalDate> parseDate(String rawDate, DateTimeFormatter formatter) {
        try {
            return Stream.of(LocalDate.parse(rawDate, formatter));
        } catch (DateTimeParseException error) {
            return Stream.empty();
        }
    }
}