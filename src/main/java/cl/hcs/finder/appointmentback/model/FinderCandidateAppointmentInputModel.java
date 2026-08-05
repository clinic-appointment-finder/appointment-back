package cl.hcs.finder.appointmentback.model;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record FinderCandidateAppointmentInputModel(
        @JsonProperty("prevision_id") @JsonAlias("previsionID") Integer previsionId,
        @JsonProperty("start_date") @JsonAlias("startDate") String startDate,
        @JsonProperty("end_date") @JsonAlias("endDate") String endDate,
        @JsonProperty("start_time") @JsonAlias("startTime") LocalTime startTime,
        @JsonProperty("end_time") @JsonAlias("endTime") LocalTime endTime,
        @JsonProperty("speciality_id") @JsonAlias("specialityID") Integer specialityId,
        @JsonProperty("doctors_ids") @JsonAlias("doctorsIds") Integer[] doctorsIds,
        @JsonProperty("office") String office) {
}
