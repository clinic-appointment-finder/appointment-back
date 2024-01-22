package cl.hcs.finder.appointmentback.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record IndisaAppointmentInputModel(@JsonAlias("prevision_id") Integer previsionID,
        @JsonAlias("start_date") String startDate,
        @JsonAlias("end_date") String endDate,
        @JsonAlias("speciality_id") Integer specialityID,
        @JsonAlias("doctors_ids") Integer[] doctorsIDs,
        @JsonAlias("office") String office,
        @JsonAlias("emails") String[] emails
        ) {
} 