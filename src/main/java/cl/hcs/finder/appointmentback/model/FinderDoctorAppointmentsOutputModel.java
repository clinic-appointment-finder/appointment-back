package cl.hcs.finder.appointmentback.model;

import java.util.List;

public record FinderDoctorAppointmentsOutputModel(
        Integer doctorId,
        List<FinderAppointmentOutputModel> appointments) {
}
