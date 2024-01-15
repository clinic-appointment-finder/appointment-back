package cl.hcs.finder.appointmentback.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record IndisaCalendarOutputModel(
        @JsonAlias("data") String[] data,
        @JsonAlias("days_to_show_offer") int daysToShowOffer,
        @JsonAlias("first_date") String firstDate,
        @JsonAlias("is_first_date") boolean isFirstDate,
        String status,
        @JsonAlias("today_str") String todayStr,
        @JsonAlias("working_dates") String[] workingDates) {
}
