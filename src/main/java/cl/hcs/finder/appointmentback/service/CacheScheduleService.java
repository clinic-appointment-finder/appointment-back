package cl.hcs.finder.appointmentback.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import cl.hcs.finder.appointmentback.common.Helper;

@Service
public class CacheScheduleService {

    private static final Logger log = Helper.getLogger();

    private final WebClient webClient;

    @Value("${indisa.app.rut}")
    private String indisaApirut;

    @Value("${indisa.app.url.referer}")
    private String indisaApiReferer;

    @Value("${indisa.app.url.path.schedule}")
    private String pathSchedule;

    public CacheScheduleService(WebClient.Builder webClientBuilder,
            @Value("${indisa.app.url}") String indisaApiURL) {
        this.webClient = webClientBuilder
                .baseUrl(indisaApiURL)
                .filter(Helper.logRequest())
                .filter(Helper.logResponse())
                .build();
    }

    @Cacheable(value = "indisaScheduleCache", key = "#codePrevision")
    public String invokeIndisaSchedule(String codePrevision) {
        String requestBody = String.format("medical_insurance=%s&cod_paciente=%s", codePrevision, indisaApirut);

        log.info("Making request to Indisa API. Schedule -> RequestBody: {}",
                requestBody);

        JsonNode result = webClient.post()
                .uri(String.format("%s/%s", pathSchedule, indisaApirut))
                .header("Accept", "*/*")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Referer", indisaApiReferer)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        // log.info("agenda result {}", result);
        return result.get("agenda_id").asText();
    }

    @CacheEvict(value = "indisaScheduleCache", allEntries = true)
    public void emptyScheduleCache() {
        log.info("vaciando agenda cache!");
    }

}
