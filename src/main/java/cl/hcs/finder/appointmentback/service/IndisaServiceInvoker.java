package cl.hcs.finder.appointmentback.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;

import org.slf4j.Logger;
import cl.hcs.finder.appointmentback.common.Helper;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Service
public class IndisaServiceInvoker {

    private static final Logger log = Helper.getLogger();

    private final WebClient webClient;

    @Value("${indisa.api.rut}")
    private String indisaApirut;

    @Value("${indisa.api.calendar.body}")
    private String indisaApiCalendarBody;

    public IndisaServiceInvoker(WebClient.Builder webClientBuilder,@Value("${indisa.api.url.calendar}") String indisaUrlApiCalendar) {
        this.webClient = webClientBuilder
                .baseUrl(indisaUrlApiCalendar)
                .filter(Helper.logRequest())
                .filter(Helper.logResponse())
                .build();
    }    

    public Mono<IndisaCalendarOutputModel> invokeExternalIndisaCalendarEndpoint(IndisaCalendarInputModel input) {
        String requestBody = indisaApiCalendarBody
                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.specialityID())
                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.doctorID())
                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.office());

        log.info("Making request to Indisa Calendar API. ScheduleID: {}, RequestBody: {}", input.scheduleID(),
                requestBody);

        return webClient.post()
                .uri("/"+ input.scheduleID())
                .header("Accept", "*/*")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(IndisaCalendarOutputModel.class);
    }

}
