package cl.hcs.finder.appointmentback.model;

import java.util.List;

public record FinderTaskAppointmentOutputModel(
        Long taskProgramId,
        List<FinderDoctorAppointmentsOutputModel> appointments) {
}
