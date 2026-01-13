package cl.hcs.finder.appointmentback.service;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.hcs.finder.appointmentback.repository.AppointmentRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AppointmentDoctorServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private AppointmentDoctorService appointmentDoctorService;

    @BeforeEach
    public void setup() {
        appointmentDoctorService = new AppointmentDoctorService(appointmentRepository);
    }

    @Test
    public void testUpdateAppointmentDoctorNotify() {
        when(appointmentRepository.updateAppointmentDoctorNotify(anyLong(), anyInt(), anyBoolean())).thenReturn(1);

        Mono<Integer> result = appointmentDoctorService.updateAppointmentDoctorNotify(1L, 123, true);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }
}
