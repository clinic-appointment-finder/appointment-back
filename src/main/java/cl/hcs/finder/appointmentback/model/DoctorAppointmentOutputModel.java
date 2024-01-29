package cl.hcs.finder.appointmentback.model;

import cl.hcs.finder.appointmentback.entity.TaskProgram;

public record DoctorAppointmentOutputModel(Long AppointmentFoundId, Integer doctorId, boolean isNotify,
        TaskProgram taskProgram, String name, String urlImage, boolean haveAgreement) {

}
