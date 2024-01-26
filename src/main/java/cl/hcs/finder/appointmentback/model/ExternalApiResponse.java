package cl.hcs.finder.appointmentback.model;

import java.util.Map;

public record ExternalApiResponse(String data, String status, Map<String, String> tags) {

 
}
