package cl.hcs.finder.appointmentback.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record IndisaAppointmentInputModel(@JsonProperty("prevision_id") @JsonAlias("previsionID") Integer previsionID,
        @JsonProperty("start_date") @JsonAlias("startDate") String startDate,
        @JsonProperty("end_date") @JsonAlias("endDate") String endDate,
        @JsonProperty("start_time") @JsonAlias("startTime") LocalTime startTime,
        @JsonProperty("end_time") @JsonAlias("endTime") LocalTime endTime,
        @JsonProperty("speciality_id") @JsonAlias("specialityID") Integer specialityID,
        @JsonProperty("doctors_ids") @JsonAlias("doctorsIDs") Integer[] doctorsIDs,
        @JsonProperty("office") String office,
        @JsonProperty("emails") String[] emails
        ) {
}
