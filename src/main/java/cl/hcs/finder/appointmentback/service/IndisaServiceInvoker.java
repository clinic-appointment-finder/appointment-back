package cl.hcs.finder.appointmentback.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import cl.hcs.finder.appointmentback.common.Helper;
import cl.hcs.finder.appointmentback.model.ExternalApiResponse;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import reactor.core.publisher.Mono;

@Service
public class IndisaServiceInvoker {

        private static final Logger log = Helper.getLogger();

        private final WebClient webClient;

        @Value("${indisa.app.rut}")
        private String indisaApirut;

        @Value("${indisa.app.url.referer}")
        private String indisaApiReferer;

        @Value("${indisa.app.url.path.calendar}")
        String indisaUrlApiPathCalendar;

        @Value("${indisa.app.calendar.body}")
        private String indisaApiCalendarBody;

        @Value("${indisa.app.body}")
        private String indisaApiCommonBody;

        @Value("${indisa.app.url.path.office}")
        private String pathOffice;

        @Value("${indisa.app.url.path.schedule}")
        private String pathSchedule;

        // 10 días en milisegundos
        private static final long TIME_VALIDATE_LONG = 864000000;

        // 1 día en milisegundos
        private static final long TIME_VALIDATE_MEDIUM = 86400000;

        // 12 horas en milisegundos
        private static final long TIME_VALIDATE_SHORT = 43200000;

        public IndisaServiceInvoker(WebClient.Builder webClientBuilder,
                        @Value("${indisa.app.url}") String indisaApiURL) {
                this.webClient = webClientBuilder
                                .baseUrl(indisaApiURL)
                                .filter(Helper.logRequest())
                                .filter(Helper.logResponse())
                                .build();
        }

        public Mono<IndisaCalendarOutputModel> invokeIndisaCalendar(IndisaCalendarInputModel input) {
                String requestBody = indisaApiCalendarBody
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.specialityID())
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.doctorID())
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.office());

                log.info("Making request to Indisa API. Calendar ->  RequestBody: {}",                                
                                requestBody);

                return webClient.post()
                                .uri(indisaUrlApiPathCalendar + "/" + invokeIndisaSchedule())
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(IndisaCalendarOutputModel.class);
        }

        @Cacheable("indisaOfficeCache")
        public List<String> invokeIndisaOffice() {
                String requestBody = indisaApiCommonBody;

                log.info("Making request to Indisa API. Office -> RequestBody: {}",
                                requestBody);

                ExternalApiResponse result = webClient.post()
                                .uri(String.format("%s/%s", pathOffice, invokeIndisaSchedule()))
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .header("Referer", indisaApiReferer)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(ExternalApiResponse.class).block();

                // log.info("result {}", result);
                String regex = "data-id=\"([^\"]+)\"";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(result.data());
                List<String> matches = new ArrayList<>();

                while (matcher.find()) {
                        matches.add(matcher.group(1));
                }
                return matches;
        }
        @Cacheable("indisaScheduleCache")
        private String invokeIndisaSchedule() {
                String requestBody = indisaApiCommonBody;

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

                log.info("agenda result {}", result);
                return result.get("agenda_id").asText();
        }

        @CacheEvict(value = "indisaOfficeCache", allEntries = true)
        @Scheduled(fixedRate = TIME_VALIDATE_LONG)
        public void emptyOfficeCache() {
                log.info("vaciando sucursales cache");
        }


        @CacheEvict(value = "indisaScheduleCache", allEntries = true)        
        public void emptyScheduleCache() {
                log.info("vaciando agenda cache!");
        }

}
