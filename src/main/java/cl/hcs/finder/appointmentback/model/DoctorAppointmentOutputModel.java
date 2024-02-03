package cl.hcs.finder.appointmentback.model;
public record DoctorAppointmentOutputModel(Long AppointmentFoundId, Integer doctorId, boolean isNotify,
        String name, String urlImage, boolean haveAgreement) {

}
