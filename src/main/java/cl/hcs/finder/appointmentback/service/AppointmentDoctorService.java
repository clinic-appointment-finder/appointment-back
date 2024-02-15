package cl.hcs.finder.appointmentback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cl.hcs.finder.appointmentback.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;

@Service
public class AppointmentDoctorService {

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentDoctorService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Mono<Integer> updateAppointmentDoctorNotify(Long taskProgramId, Integer doctorId, boolean isNotify) {
        return Mono.fromCallable(
                () -> appointmentRepository.updateAppointmentDoctorNotify(taskProgramId, doctorId, isNotify));
    }

}
