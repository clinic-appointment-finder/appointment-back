package cl.hcs.finder.appointmentback.model;

import java.util.List;

public record IndisaProposeHoursOutputModel(
        String date,
        List<String> hours,
        String status) {
}
