package cl.hcs.finder.appointmentback.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record FinderAppointmentOutputModel(
        @JsonIgnore Integer doctorId,
        String date,
        List<String> hours) {
}
