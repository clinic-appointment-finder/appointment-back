package cl.hcs.finder.appointmentback.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.hcs.finder.appointmentback.common.Helper;
import cl.hcs.finder.appointmentback.model.ExternalApiResponseModel;
import cl.hcs.finder.appointmentback.model.GenericOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.model.IndisaProposeHoursInputModel;
import cl.hcs.finder.appointmentback.model.IndisaProposeHoursApiResponseModel;
import cl.hcs.finder.appointmentback.model.IndisaProposeHoursOutputModel;
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel;
import reactor.core.publisher.Flux;
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

        @Value("${indisa.app.url.path.propose-hours}")
        private String indisaUrlApiPathProposeHours;

        @Value("${indisa.app.calendar.body}")
        private String indisaApiCalendarBody;

        @Value("${indisa.app.body}")
        private String indisaApiCommonBody;

        @Value("${indisa.app.url.path.office}")
        private String pathOffice;

        @Value("${indisa.app.url.path.speciality}")
        private String pathSpeciality;

        @Value("${indisa.app.url.path.doctors}")
        private String pathDoctors;

        @Value("${indisa.app.url.path.doctors.image.default}")
        private String urlDefaultImage;

        @Value("${indisa.app.url.path.prevision}")
        private String pathPrevison;

        // 10 días en milisegundos
        private static final long TIME_VALIDATE_LONG = 864000000;

        // 1 día en milisegundos
        private static final long TIME_VALIDATE_MEDIUM = 86400000;

        // 12 horas en milisegundos
        private static final long TIME_VALIDATE_SHORT = 43200000;

        @Autowired
        private CacheScheduleService cacheService;

        public IndisaServiceInvoker(WebClient.Builder webClientBuilder,
                        @Value("${indisa.app.url}") String indisaApiURL) {
                this.webClient = webClientBuilder
                                .baseUrl(indisaApiURL)
                                .filter(Helper.logRequest())
                                .filter(Helper.logResponse())
                                .build();
        }

        public Mono<IndisaCalendarOutputModel> invokeIndisaCalendar(IndisaCalendarInputModel input) {
                return invokeIndisaCalendar(input, 0, 0);
        }

        public Mono<IndisaCalendarOutputModel> invokeIndisaCalendar(IndisaCalendarInputModel input,
                        int month, int year) {
                String requestBody = indisaApiCalendarBody
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.specialityID())
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.doctorID())
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", input.office())
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", String.valueOf(month))
                                .replaceFirst("<<<PUT_VALUE_HERE>>>", String.valueOf(year));

                log.info("Making request to Indisa API. Calendar ->  RequestBody: {}", requestBody);

                return cacheService.invokeIndisaSchedule(input.previsionID())
                                .flatMap(schedule -> webClient.post()
                                                .uri(indisaUrlApiPathCalendar + "/" + schedule)
                                                .header("Accept", "*/*")
                                                .header("Content-Type",
                                                                "application/x-www-form-urlencoded; charset=UTF-8")
                                                .bodyValue(requestBody)
                                                .retrieve()
                                                 .bodyToMono(IndisaCalendarOutputModel.class));
        }

        public Mono<IndisaProposeHoursOutputModel> invokeIndisaProposeHours(
                        IndisaProposeHoursInputModel input) {
                String requestBody = String.format(
                                "spec_id=%s&doc_id=%s&office_id=%s&date=%s&source=browser",
                                input.specialityID(), input.doctorID(), input.office(), input.date());

                log.info("Making request to Indisa API. Propose hours -> RequestBody: {}", requestBody);

                return cacheService.invokeIndisaSchedule(input.previsionID())
                                .flatMap(schedule -> webClient.post()
                                                .uri(indisaUrlApiPathProposeHours + "/" + schedule)
                                                .header("Accept", "*/*")
                                                .header("Content-Type",
                                                                "application/x-www-form-urlencoded; charset=UTF-8")
                                                .header("Referer", indisaApiReferer)
                                                .header("X-Requested-With", "XMLHttpRequest")
                                                .bodyValue(requestBody)
                                                .retrieve()
                                                .bodyToMono(IndisaProposeHoursApiResponseModel.class)
                                                .map(response -> new IndisaProposeHoursOutputModel(
                                                                input.date(), extractAvailableHours(response.data()),
                                                                response.status())));
        }

        @Cacheable("indisaOfficeCache")
        public Mono<List<String>> invokeIndisaOffice(String codePrevision) {
                String requestBody = indisaApiCommonBody;
                log.info("Making request to Indisa API. Office -> RequestBody: {}", requestBody);
                return cacheService.invokeIndisaSchedule(codePrevision)
                                .flatMap(schedule -> {
                                        return webClient.post()
                                                        .uri(String.format("%s/%s", pathOffice, schedule))
                                                        .header("Accept", "*/*")
                                                        .header("Content-Type",
                                                                        "application/x-www-form-urlencoded; charset=UTF-8")
                                                        .header("Referer", indisaApiReferer)
                                                        .bodyValue(requestBody)
                                                        .retrieve()
                                                        .bodyToMono(ExternalApiResponseModel.class)
                                                        .map(result -> {
                                                                String regex = "data-id=\"([^\"]+)\"";
                                                                Pattern pattern = Pattern.compile(regex);
                                                                Matcher matcher = pattern.matcher(result.data());
                                                                List<String> matches = new ArrayList<>();
                                                                while (matcher.find()) {
                                                                        matches.add(matcher.group(1));
                                                                }
                                                                return matches;
                                                        });
                                });
        }

        @Cacheable("indisaPrevisionCache")
        public Flux<GenericOutputModel> invokeIndisaPrevision() {
                log.info("Making request to Indisa API. Prevision");

                return webClient.get()
                                .uri(pathPrevison)
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/json")
                                .header("Referer", indisaApiReferer)
                                .retrieve()
                                .bodyToFlux(JsonNode.class)
                                .flatMap(result -> {
                                        if (result != null && result.get("data").isArray()) {
                                                Iterator<JsonNode> elements = result.get("data").elements();
                                                List<GenericOutputModel> outputModels = new ArrayList<>();

                                                while (elements.hasNext()) {
                                                        JsonNode element = elements.next();
                                                        GenericOutputModel outputModel = new ObjectMapper()
                                                                        .convertValue(element,
                                                                                        GenericOutputModel.class);
                                                        outputModels.add(outputModel);
                                                }
                                                return Flux.fromIterable(outputModels);
                                        } else {
                                                return Flux.empty();
                                        }
                                });
        }

        @Cacheable("indisaSpecialityCache")
        public Mono<List<GenericOutputModel>> invokeIndisaSpeciality(String codePrevision, String office) {
                String requestBody = indisaApiCommonBody.replace("isapre=", "isapre=" + codePrevision)
                                .replace("office_id=", "office_id=" + office);
                System.out.println("pathSpecialitypathSpecialitypathSpecialitypathSpecialitypathSpeciality"
                                + pathSpeciality);
                log.info("Making request to Indisa API. Speciality -> RequestBody: {}",
                                requestBody);
                // Invocar a cacheService.invokeIndisaSchedule(codePrevision) y continuar de
                // forma reactiva
                return cacheService.invokeIndisaSchedule(codePrevision)
                                .flatMap(schedule -> {
                                        log.info("Schedule ID: " + schedule);
                                        // Realizar la llamada a la API de Indisa de forma reactiva
                                        return webClient.post()
                                                        .uri(String.format("%s/%s", pathSpeciality, schedule))
                                                        .header("Accept", "*/*")
                                                        .header("Content-Type",
                                                                        "application/x-www-form-urlencoded; charset=UTF-8")
                                                        .header("Referer", indisaApiReferer)
                                                        .bodyValue(requestBody)
                                                        .retrieve()
                                                        .bodyToMono(JsonNode.class)
                                                        .map(result -> extractSpecialities(
                                                                        result.get("data").asText()));
                                });

        }

        @Cacheable("indisaDoctorsCache")
        public Mono<MedicalAgreementModel> invokeIndisaDoctors(String codePrevision, String office,
                        String codeSpeciality) {
                String requestBody = indisaApiCommonBody.replace("isapre=", "isapre=" + codePrevision)
                                .replace("office_id=", "office_id=" + office)
                                .replace("spec_id=", "spec_id=" + codeSpeciality);

                log.info("Making request to Indisa API. Doctors -> RequestBody: {}", requestBody);

                return cacheService.invokeIndisaSchedule(codePrevision)
                                .flatMap(schedule -> {
                                        return webClient.post()
                                                        .uri(String.format("%s/%s", pathDoctors, schedule))
                                                        .header("Accept", "*/*")
                                                        .header("Content-Type",
                                                                        "application/x-www-form-urlencoded; charset=UTF-8")
                                                        .header("Referer", indisaApiReferer)
                                                        .bodyValue(requestBody)
                                                        .retrieve()
                                                        .bodyToMono(JsonNode.class)
                                                        .map(result -> extractMedicos(result.path("data").asText("")));
                                });
        }

        @CacheEvict(value = "indisaOfficeCache", allEntries = true)
        @Scheduled(fixedRate = TIME_VALIDATE_LONG)
        public void emptyOfficeCache() {
                log.info("vaciando sucursales cache");
        }

        @CacheEvict(value = "indisaPrevisionCache", allEntries = true)
        @Scheduled(fixedRate = TIME_VALIDATE_LONG)
        public void emptyPrevisionCache() {
                log.info("vaciando previsión cache!");
        }

        @CacheEvict(value = "indisaSpecialityCache", allEntries = true)
        @Scheduled(fixedRate = TIME_VALIDATE_MEDIUM)
        public void emptySpecialityCache() {
                log.info("vaciando especialidades cache!");
        }

        @CacheEvict(value = "indisaDoctorsCache", allEntries = true)
        @Scheduled(fixedRate = TIME_VALIDATE_SHORT)
        public void emptyDoctorsCache() {
                log.info("vaciando doctors cache!");
        }

        private static List<GenericOutputModel> extractSpecialities(String data) {
                String regex = "<li[^>]*>\\s*<a[^>]*class=\"especialidad\"[^>]*data-id=\"([^\"]*)\"[^>]*data-type=\"speciality\"[^>]*>(.*?)<\\/a>\\s*<\\/li>";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(data);

                List<GenericOutputModel> specialities = new ArrayList<>();

                while (matcher.find()) {
                        String dataId = matcher.group(1);
                        String text = matcher.group(2);
                        GenericOutputModel speciality = new GenericOutputModel(dataId, text.trim());
                        specialities.add(speciality);
                }

                return specialities;
        }

        static List<String> extractAvailableHours(String data) {
                if (data == null || data.isBlank()) {
                        return Collections.emptyList();
                }

                Set<String> hours = new LinkedHashSet<>();
                Pattern selectedDatePattern = Pattern.compile(
                                "get_date_selected\\('(?:[^']* )?([0-9]{1,2}:[0-9]{2})'\\)");
                Matcher selectedDateMatcher = selectedDatePattern.matcher(data);
                while (selectedDateMatcher.find()) {
                        hours.add(selectedDateMatcher.group(1));
                }

                Pattern hourPattern = Pattern.compile("<span[^>]*class=\"hour\"[^>]*>\\s*([0-9]{1,2}:[0-9]{2})");
                Matcher hourMatcher = hourPattern.matcher(data);
                while (hourMatcher.find()) {
                        hours.add(hourMatcher.group(1));
                }

                return List.copyOf(hours);
        }

        static List<String> extractAvailableHours(JsonNode data) {
                if (data == null || data.isNull() || data.isMissingNode()) {
                        return Collections.emptyList();
                }
                if (data.isTextual()) {
                        return extractAvailableHours(data.asText());
                }
                if (data.isObject()) {
                        for (String fieldName : List.of("html", "content", "data")) {
                                JsonNode nestedData = data.get(fieldName);
                                if (nestedData != null && nestedData.isTextual()) {
                                        return extractAvailableHours(nestedData.asText());
                                }
                        }
                }
                return Collections.emptyList();
        }

        private MedicalAgreementModel extractMedicos(String data) {
                List<MedicalAgreementModel.DoctorModel> whithList = new ArrayList<>();
                List<MedicalAgreementModel.DoctorModel> whithoutList = new ArrayList<>();

                Pattern doctorBlockPattern = Pattern.compile(
                                "<li[^>]*class=\"has_agenda_(\\w+)[^\"]*\"[^>]*>(.*?)</li>",
                                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
                Matcher doctorBlockMatcher = doctorBlockPattern.matcher(data);

                while (doctorBlockMatcher.find()) {
                        String agenda = doctorBlockMatcher.group(1);
                        String doctorBlock = doctorBlockMatcher.group(2);

                        String urlImagen = extractGroup(doctorBlock,
                                        "<img[^>]*src=\"([^\"]+)\"",
                                        1);
                        String conveniosData = extractGroup(doctorBlock,
                                        "data-convenios\\s*=\\s*\"([^\"]*)\"",
                                        1);
                        String code = extractGroup(doctorBlock,
                                        "data-id=\"([^\"]+)\"",
                                        1);
                        String name = extractGroup(doctorBlock,
                                        "<span[^>]*class=\"doctor-name\"[^>]*>(.*?)</span>",
                                        1);

                        if (code == null || name == null) {
                                continue;
                        }

                        boolean tieneConvenio = conveniosData.trim().isEmpty();
                        String imageUrl = urlImagen.startsWith("https") ? urlImagen
                                        : (urlDefaultImage != null
                                                        ? urlDefaultImage
                                                        : "");
                        Optional<Boolean> haveSchedule = Optional.of(agenda.trim().equalsIgnoreCase("true"));
                        MedicalAgreementModel.DoctorModel medico = new MedicalAgreementModel.DoctorModel(code, name,
                                        imageUrl, haveSchedule);

                        // log.info("tiene convenio: " + tieneConvenio);
                        if (tieneConvenio) {
                                whithList.add(medico);
                        } else {
                                whithoutList.add(medico);
                        }
                }
                return new MedicalAgreementModel(whithList, whithoutList);
        }

        private String extractGroup(String input, String regex, int group) {
                Matcher matcher = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE).matcher(input);
                if (!matcher.find()) {
                        return "";
                }
                return matcher.group(group).replaceAll("\\s+", " ").trim();
        }

}
