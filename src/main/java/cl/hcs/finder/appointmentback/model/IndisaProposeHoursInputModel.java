package cl.hcs.finder.appointmentback.model;

public record IndisaProposeHoursInputModel(
        String specialityID,
        String doctorID,
        String office,
        String previsionID,
        String date) {
}
