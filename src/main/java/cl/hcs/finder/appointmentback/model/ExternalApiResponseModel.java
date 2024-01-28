package cl.hcs.finder.appointmentback.model;

import java.util.Map;

public record ExternalApiResponseModel(String data, String status, Map<String, String> tags) {

 
}
