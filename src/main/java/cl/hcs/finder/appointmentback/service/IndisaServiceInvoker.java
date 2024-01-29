package cl.hcs.finder.appointmentback.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
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
import cl.hcs.finder.appointmentback.model.MedicalAgreementModel;
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

        @Value("${indisa.app.url.path.speciality}")
        private String pathSpeciality;

        @Value("${indisa.app.url.path.doctors}")
        private String pathDoctors;

        @Value("${indisa.app.url.path.doctors.image.default}")
        private String urlDefaultImage;

        @Value("${indisa.app.url.path.prevision}")
        private String pathPrevison;

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
                                .uri(indisaUrlApiPathCalendar + "/" + invokeIndisaSchedule(input.previsionID()))
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(IndisaCalendarOutputModel.class);
        }

        @Cacheable("indisaOfficeCache")
        public List<String> invokeIndisaOffice(String codePrevision) {
                String requestBody = indisaApiCommonBody;

                log.info("Making request to Indisa API. Office -> RequestBody: {}",
                                requestBody);

                ExternalApiResponseModel result = webClient.post()
                                .uri(String.format("%s/%s", pathOffice, invokeIndisaSchedule(codePrevision)))
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .header("Referer", indisaApiReferer)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(ExternalApiResponseModel.class).block();

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

        @Cacheable("indisaPrevisionCache")
        public List<GenericOutputModel> invokeIndisaPrevision() {

                log.info("Making request to Indisa API. Prevision");
                JsonNode result = webClient.get()
                                .uri(String.format("%s", pathPrevison))
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/json")
                                .header("Referer", indisaApiReferer)
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .block();

                // log.info("agenda result {}", result);

                List<GenericOutputModel> outputModels = new ArrayList<>();

                // Verifica si "data" es un array
                if (result != null && result.get("data").isArray()) {
                        Iterator<JsonNode> elements = result.get("data").elements();

                        // Itera sobre los elementos del array y mapea cada elemento a un
                        // GenericOutputModel
                        while (elements.hasNext()) {
                                JsonNode element = elements.next();
                                GenericOutputModel outputModel = new ObjectMapper().convertValue(element,
                                                GenericOutputModel.class);
                                outputModels.add(outputModel);
                        }
                }

                return outputModels;
        }

        @Cacheable("indisaSpecialityCache")
        public List<GenericOutputModel> invokeIndisaSpeciality(String codePrevision, String office) {
                String requestBody = indisaApiCommonBody.replace("isapre=", "isapre=" + codePrevision)
                                .replace("office_id=", "office_id=" + office);

                log.info("Making request to Indisa API. Speciality -> RequestBody: {}",
                                requestBody);
                JsonNode result = webClient.post()
                                .uri(String.format("%s/%s", pathSpeciality, invokeIndisaSchedule(codePrevision)))
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .header("Referer", indisaApiReferer)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .block();

                // log.info("agenda result {}", result);
                return extractSpecialities(result.get("data").asText());
        }

        @Cacheable("indisaDoctorsCache")
        public MedicalAgreementModel invokeIndisaDoctors(String codePrevision, String office,
                        String codeSpeciality) {
                String requestBody = indisaApiCommonBody.replace("isapre=", "isapre=" + codePrevision)
                                .replace("office_id=", "office_id=" + office)
                                .replace("spec_id=", "spec_id=" + codeSpeciality);

                log.info("Making request to Indisa API. Doctors -> RequestBody: {}",
                                requestBody);
                JsonNode result = webClient.post()
                                .uri(String.format("%s/%s", pathDoctors, invokeIndisaSchedule(codePrevision)))
                                .header("Accept", "*/*")
                                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                .header("Referer", indisaApiReferer)
                                .bodyValue(requestBody)
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .block();

                // log.info("agenda result {}", result);
                return extractMedicos(result.get("data").asText());
        }

        @Cacheable("indisaScheduleCache")
        private String invokeIndisaSchedule(String codePrevision) {
                String requestBody = String.format("medical_insurance=%s&cod_paciente=%s",codePrevision, indisaApirut);

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

        @CacheEvict(value = "indisaScheduleCache", allEntries = true)
        public void emptyScheduleCache() {
                log.info("vaciando agenda cache!");
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

        private MedicalAgreementModel extractMedicos(String data) {
                List<MedicalAgreementModel.DoctorModel> whithList = new ArrayList<>();
                List<MedicalAgreementModel.DoctorModel> whithoutList = new ArrayList<>();

                Pattern regexMedicos = Pattern.compile(
                                "<li class=\"has_agenda_(\\w+)\\s*\"[^>]*>\\s*<img src=\"([^\"]+)\"[^>]*>\\s*<a[^>]*data-convenios\\s*=\\s*\"([^\"]*)\"[^>]*data-id=\"([^\"]+)\"[^>]*>\\s*<span class=\"doctor-name\">([^<]+)<\\/span>",
                                Pattern.MULTILINE);
                Matcher medicoMatcher = regexMedicos.matcher(data);
                while (medicoMatcher.find()) {
                        String agenda = medicoMatcher.group(1);
                        String urlImagen = medicoMatcher.group(2);
                        String conveniosData = medicoMatcher.group(3);
                        String code = medicoMatcher.group(4);
                        String name = medicoMatcher.group(5);

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

}
