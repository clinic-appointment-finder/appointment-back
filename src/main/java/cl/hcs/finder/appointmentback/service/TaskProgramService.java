package cl.hcs.finder.appointmentback.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import cl.hcs.finder.appointmentback.entity.AppointmentFound;
import cl.hcs.finder.appointmentback.entity.Clinic;
import cl.hcs.finder.appointmentback.entity.TaskProgram;
import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.repository.TaskProgramRepository;
import jakarta.transaction.Transactional;

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

    public Page<TaskProgram> FindAll(int page, int size, Boolean isTaskValidate, Boolean isActive, String office) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate"));
        // Construir las especificaciones de búsqueda
        Specification<TaskProgram> spec = Specification.where(null);
        if (isActive != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("active"), isActive));
        }
        if (office != null && !office.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("officeName"), office));
        }
        if (isTaskValidate != null) {
            if (isTaskValidate) {
                // Si es true, la fecha actual debe estar entre la fecha desde y hasta
                spec = spec.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.and(
                                criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), LocalDate.now()),
                                criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), LocalDate.now())
                        )
                );
            } else {
                // Si es false, la fecha actual NO debe estar entre la fecha desde y hasta
                spec = spec.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.or(
                                criteriaBuilder.lessThan(root.get("endDate"), LocalDate.now()),
                                criteriaBuilder.greaterThan(root.get("startDate"), LocalDate.now())
                        )
                );
            }
        }        
        return taskProgramRepository.findAll(spec, pageable);
    }

    public Optional<TaskProgram> FindByID(@NonNull Long id) {
        return taskProgramRepository.findById(id);
    }

    @Transactional
    public int updateTaskProgramActive(Long id, boolean active) {
        return taskProgramRepository.updateTaskProgramActive(id, active);
    }

}