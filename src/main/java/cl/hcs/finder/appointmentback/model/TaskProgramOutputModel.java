package cl.hcs.finder.appointmentback.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import cl.hcs.finder.appointmentback.entity.Clinic;

public record TaskProgramOutputModel(Long taskProgramId, Clinic clinic, List<DoctorAppointmentOutputModel> doctors,
        GenericOutputModel prevision, LocalDate startDate, LocalDate endDate, String office, GenericOutputModel speciality,
        List<String> emails, LocalDateTime creationDate, boolean isActive) {

}
