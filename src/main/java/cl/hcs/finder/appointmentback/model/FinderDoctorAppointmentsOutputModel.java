package cl.hcs.finder.appointmentback.model;

import java.util.List;

public record FinderDoctorAppointmentsOutputModel(
        Long appointmentFoundId,
        Integer doctorId,
        boolean isNotify,
        String name,
        String urlImage,
        boolean haveAgreement,
        List<FinderAppointmentOutputModel> appointments) {
}