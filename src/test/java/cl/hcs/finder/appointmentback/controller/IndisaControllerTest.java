package cl.hcs.finder.appointmentback.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.hcs.finder.appointmentback.entity.Clinic;
import cl.hcs.finder.appointmentback.entity.TaskProgram;
import cl.hcs.finder.appointmentback.model.GenericOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel;
import cl.hcs.finder.appointmentback.model.TaskProgramOutputModel;
import cl.hcs.finder.appointmentback.service.AppointmentDoctorService;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import cl.hcs.finder.appointmentback.service.TaskProgramService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class IndisaControllerTest {

    @Mock
    private TaskProgramService taskProgramService;

    @Mock
    private IndisaServiceInvoker indisaServiceInvoker;

    @Mock
    private AppointmentDoctorService appointmentDoctorService;

    private IndisaController indisaController;

    @BeforeEach
    public void setup() {
        indisaController = new IndisaController(appointmentDoctorService, taskProgramService, indisaServiceInvoker);
    }

    @Test
    public void testCreateTaskProgram() {
        IndisaAppointmentInputModel inputModel = new IndisaAppointmentInputModel(67, "2024-01-15", "2024-01-20", 226,
                new Integer[] { 14655 }, "PROVIDENCIA", new String[] { "test@example.com" });
        TaskProgram expectedTaskProgram = new TaskProgram();
        expectedTaskProgram.setTaskProgramId(1L);

        when(taskProgramService.createTaskProgram(any(IndisaAppointmentInputModel.class)))
                .thenReturn(Mono.just(expectedTaskProgram));

        Mono<ResponseEntity<TaskProgram>> result = indisaController.createTaskProgram(inputModel);

        StepVerifier.create(result)
                .expectNextMatches(
                        response -> response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null)
                .verifyComplete();
    }

    @Test
    public void testFindAllTaskProgram() {
        TaskProgram taskProgram = new TaskProgram();
        taskProgram.setTaskProgramId(1L);
        taskProgram.setPrevisionId(67);
        taskProgram.setOfficeName("PROVIDENCIA");
        taskProgram.setSpecialityId(226);
        taskProgram.setDoctors(new ArrayList<>());
        taskProgram.setEmails(Arrays.asList("test@example.com"));
        taskProgram.setCreationDate(LocalDateTime.now());
        taskProgram.setActive(true);
        taskProgram.setStartDate(java.time.LocalDate.now());
        taskProgram.setEndDate(java.time.LocalDate.now().plusDays(5));
        taskProgram.setClinic(new Clinic(1, "Clínica Indisa"));

        List<TaskProgram> taskProgramList = Collections.singletonList(taskProgram);
        Page<TaskProgram> pageTask = new PageImpl<>(taskProgramList);

        when(taskProgramService.FindAll(anyInt(), anyInt(), any(), any(), any(), anyBoolean()))
                .thenReturn(Mono.just(pageTask));

        // Mocking transforms
        when(indisaServiceInvoker.invokeIndisaSpeciality(anyString(), anyString()))
                .thenReturn(Mono.just(Collections.singletonList(new GenericOutputModel("226", "Speciality"))));
        when(indisaServiceInvoker.invokeIndisaPrevision())
                .thenReturn(Flux.just(new GenericOutputModel("67", "Prevision")));
        when(indisaServiceInvoker.invokeIndisaDoctors(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(new MedicalAgreementModel(new ArrayList<>(), new ArrayList<>())));

        Mono<ResponseEntity<List<TaskProgramOutputModel>>> result = indisaController.findAllTaskProgram(0, 5, false,
                true, "PROVIDENCIA", true);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody() != null
                        && !response.getBody().isEmpty())
                .verifyComplete();
    }

    @Test
    public void testGetTaskProgramById() {
        TaskProgram taskProgram = new TaskProgram();
        taskProgram.setTaskProgramId(1L);
        taskProgram.setPrevisionId(67);
        taskProgram.setOfficeName("PROVIDENCIA");
        taskProgram.setSpecialityId(226);
        taskProgram.setDoctors(new ArrayList<>());
        taskProgram.setEmails(Arrays.asList("test@example.com"));
        taskProgram.setStartDate(java.time.LocalDate.now());
        taskProgram.setEndDate(java.time.LocalDate.now().plusDays(5));
        taskProgram.setClinic(new Clinic(1, "Clínica Indisa"));

        when(taskProgramService.FindByID(anyLong())).thenReturn(Mono.just(taskProgram));

        // Mocking transforms
        when(indisaServiceInvoker.invokeIndisaSpeciality(anyString(), anyString()))
                .thenReturn(Mono.just(Collections.singletonList(new GenericOutputModel("226", "Speciality"))));
        when(indisaServiceInvoker.invokeIndisaPrevision())
                .thenReturn(Flux.just(new GenericOutputModel("67", "Prevision")));
        when(indisaServiceInvoker.invokeIndisaDoctors(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(new MedicalAgreementModel(new ArrayList<>(), new ArrayList<>())));

        Mono<ResponseEntity<TaskProgramOutputModel>> result = indisaController.getTaskProgramById(1L);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                .verifyComplete();
    }

    @Test
    public void testUpdateTaskProgramActive() {
        when(taskProgramService.updateTaskProgramActive(anyLong(), anyBoolean())).thenReturn(Mono.just(1));

        Mono<ResponseEntity<Void>> result = indisaController.updateTaskProgramActive(1L,
                Collections.singletonMap("isActive", true));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }

    @Test
    public void testUpdateDoctorNotify() {
        when(appointmentDoctorService.updateAppointmentDoctorNotify(anyLong(), anyInt(), anyBoolean()))
                .thenReturn(Mono.just(1));

        Mono<ResponseEntity<HttpStatus>> result = indisaController.updateDoctorNotify(1L, 123,
                Collections.singletonMap("isNotify", true));

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK)
                .verifyComplete();
    }
}
