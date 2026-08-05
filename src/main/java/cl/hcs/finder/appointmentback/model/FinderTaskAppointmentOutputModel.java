package cl.hcs.finder.appointmentback.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record FinderTaskAppointmentOutputModel(
        Long taskProgramId,
        String clinicName,
        List<String> emails,
        GenericOutputModel prevision,
        GenericOutputModel speciality,
        String office,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        LocalDateTime creationDate,
        boolean isActive,
        List<FinderDoctorAppointmentsOutputModel> appointments) {
}