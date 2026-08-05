package cl.hcs.finder.appointmentback.model;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public record IndisaProposeHoursApiResponseModel(
        JsonNode data,
        String status,
        Map<String, String> tags) {
}
