package cl.hcs.finder.appointmentback.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import cl.hcs.finder.appointmentback.entity.AppointmentFound;
import cl.hcs.finder.appointmentback.entity.Clinic;
import cl.hcs.finder.appointmentback.entity.TaskProgram;
import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.repository.TaskProgramRepository;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;

@Service
public class TaskProgramService {

    private final TaskProgramRepository taskProgramRepository;

    @Autowired
    public TaskProgramService(TaskProgramRepository taskProgramRepository) {
        this.taskProgramRepository = taskProgramRepository;
    }

    public TaskProgram createTaskProgram(IndisaAppointmentInputModel inputModel) {
        TaskProgram taskProgram = new TaskProgram();
        taskProgram.setActive(true);
        taskProgram.setCreationDate(LocalDateTime.now());
        taskProgram.setOfficeName(inputModel.office());
        taskProgram.setClinic(new Clinic(1, null));
        for (Integer element : inputModel.doctorsIDs()) {
            AppointmentFound doc = new AppointmentFound();
            doc.setDoctorId(element);
            doc.setNotify(false);
            taskProgram.addDoctor(doc);
        }
        taskProgram.setEmails(Arrays.asList(inputModel.emails()));
        taskProgram.setPrevisionId(inputModel.previsionID());
        taskProgram.setSpecialityId(inputModel.specialityID());
        // formato de la cadena de fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        taskProgram.setStartDate(LocalDate.parse(inputModel.startDate(), formatter));
        taskProgram.setEndDate(LocalDate.parse(inputModel.endDate(), formatter));
        // Setear los valores en taskProgram con los valores de inputModel
        return taskProgramRepository.save(taskProgram);
    }

    public Mono<Page<TaskProgram>> FindAll(int page, int size, Boolean isTaskValidate, Boolean isActive, String office,
            Boolean obfuscateMail) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate"));
        // Construir las especificaciones de búsqueda
        Specification<TaskProgram> spec = Specification.where(null);
        if (isActive != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("active"), isActive));
        }
        if (office != null && !office.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("officeName"), office));
        }
        if (isTaskValidate != null) {
            if (isTaskValidate) {
                // Si es true, la fecha actual debe estar entre la fecha desde y hasta
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.and(
                        criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), LocalDate.now()),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), LocalDate.now())));
            } else {
                // Si es false, la fecha actual NO debe estar entre la fecha desde y hasta
                spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                        criteriaBuilder.lessThan(root.get("endDate"), LocalDate.now()),
                        criteriaBuilder.greaterThan(root.get("startDate"), LocalDate.now())));
            }
        }
        Page<TaskProgram> taskPrograms = taskProgramRepository.findAll(spec, pageable);

        if (obfuscateMail)
            taskPrograms.getContent().forEach(this::obfuscateEmails);

        return Mono.just(taskPrograms);
    }

    public Mono<ResponseEntity<?>> FindByID(Long id) {
        return Mono.fromSupplier(() -> taskProgramRepository.findById(id))
                .flatMap(optional -> optional.map(taskProgram -> {
                    obfuscateEmails(taskProgram);
                    return ResponseEntity.ok(taskProgram);
                }).map(Mono::just)
                .orElse(Mono.just(ResponseEntity.notFound().build())));
    }

    @Transactional
    public int updateTaskProgramActive(Long id, boolean active) {
        return taskProgramRepository.updateTaskProgramActive(id, active);
    }

    private void obfuscateEmails(TaskProgram taskProgram) {
        List<String> obfuscatedEmails = taskProgram.getEmails().stream()
                .map(email -> obfuscateEmail(email))
                .collect(Collectors.toList());

        taskProgram.setEmails(obfuscatedEmails);
    }

    private String obfuscateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String prefix = email.substring(0, atIndex);
            String domain = email.substring(atIndex);

            if (prefix.length() >= 6) {
                return prefix.substring(0, 2) + "xxx" + prefix.substring(prefix.length() - 2, prefix.length())
                        + domain;
            } else {
                return prefix.substring(0, 1) + "xxx" + prefix.substring(prefix.length() - 1, prefix.length())
                        + domain;
            }
        }
        return email;
    }

}