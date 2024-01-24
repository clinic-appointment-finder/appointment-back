package cl.hcs.finder.appointmentback.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import cl.hcs.finder.appointmentback.model.AppointmentFound;
import cl.hcs.finder.appointmentback.model.Clinic;
import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.model.TaskProgram;
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

    
    public List<TaskProgram> FindAll() {   
        Sort sortByCreationDate = Sort.by(Sort.Direction.DESC, "creationDate");
        return taskProgramRepository.findAll(sortByCreationDate);
    }

    public Optional<TaskProgram> FindByID(@NonNull Long id) {   
        return taskProgramRepository.findById(id);
    }
    
    @Transactional
    public int updateTaskProgramActive(Long id, boolean active) {
        return taskProgramRepository.updateTaskProgramActive(id, active);
    }
    
}