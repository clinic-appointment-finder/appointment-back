package cl.hcs.finder.appointmentback.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record IndisaCalendarInputModel(
        @JsonAlias("schedule_id") String scheduleID,
        @JsonAlias("speciality_id") String specialityID,
        @JsonAlias("doctor_id") String doctorID,
        @JsonAlias("office") String office) {
}
