package cl.hcs.finder.appointmentback.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cl.hcs.finder.appointmentback.model.GenericOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class IndisaExternalControllerTest {

    @Mock
    private IndisaServiceInvoker externalServiceInvoker;

    private IndisaExternalController indisaExternalController;

    @BeforeEach
    public void setup() {
        indisaExternalController = new IndisaExternalController(externalServiceInvoker);
    }

    @Test
    public void testInvokeExternalService() {
        IndisaCalendarOutputModel outputModel = new IndisaCalendarOutputModel(new String[] {}, 0, "2024-01-01", true,
                "OK", "2024-01-01", new String[] {});
        when(externalServiceInvoker.invokeIndisaCalendar(any(IndisaCalendarInputModel.class)))
                .thenReturn(Mono.just(outputModel));

        Mono<ResponseEntity<IndisaCalendarOutputModel>> result = indisaExternalController.invokeExternalService("226",
                "14655", "PROVIDENCIA", "67");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                .verifyComplete();
    }

    @Test
    public void testInvokeExternalServiceOffice() {
        List<String> offices = Arrays.asList("PROVIDENCIA");
        when(externalServiceInvoker.invokeIndisaOffice(anyString())).thenReturn(Mono.just(offices));

        Mono<ResponseEntity<List<String>>> result = indisaExternalController.invokeExternalServiceOffice("67");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                .verifyComplete();
    }

    @Test
    public void testInvokeExternalServicePrevision() {
        GenericOutputModel prevision = new GenericOutputModel("67", "Prevision");
        when(externalServiceInvoker.invokeIndisaPrevision()).thenReturn(Flux.just(prevision));

        Mono<ResponseEntity<?>> result = indisaExternalController.invokeExternalServicePrevision();

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                .verifyComplete();
    }

    @Test
    public void testInvokeExternalServiceSpeciality() {
        GenericOutputModel speciality = new GenericOutputModel("226", "Speciality");
        when(externalServiceInvoker.invokeIndisaSpeciality(anyString(), anyString()))
                .thenReturn(Mono.just(Collections.singletonList(speciality)));

        Mono<ResponseEntity<List<GenericOutputModel>>> result = indisaExternalController
                .invokeExternalServiceSpeciality("67", "PROVIDENCIA");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                .verifyComplete();
    }

    @Test
    public void testInvokeExternalServiceDoctors() {
        MedicalAgreementModel medicalAgreementModel = new MedicalAgreementModel(new ArrayList<>(), new ArrayList<>());
        // Adding a dummy doctor to make it not empty so it returns OK
        MedicalAgreementModel.DoctorModel doctor = new MedicalAgreementModel.DoctorModel("1", "Doctor", "url",
                java.util.Optional.empty());
        List<MedicalAgreementModel.DoctorModel> withList = new ArrayList<>();
        withList.add(doctor);
        MedicalAgreementModel nonEmptyModel = new MedicalAgreementModel(withList, new ArrayList<>());

        when(externalServiceInvoker.invokeIndisaDoctors(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(nonEmptyModel));

        Mono<ResponseEntity<MedicalAgreementModel>> result = indisaExternalController.invokeExternalServiceDoctors("67",
                "PROVIDENCIA", "226");

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK && response.getBody() != null)
                .verifyComplete();
    }
}
