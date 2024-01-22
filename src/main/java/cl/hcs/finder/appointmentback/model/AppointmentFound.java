package cl.hcs.finder.appointmentback.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class AppointmentFound {
    
    @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long AppointmentFoundId;

    private Integer doctorId;

    private boolean isNotify;
    
    @ManyToOne
    @JoinColumn(name = "task_program_id")
    @JsonBackReference 
    private TaskProgram taskProgram;

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setNotify(boolean isNotify) {
        this.isNotify = isNotify;
    }

    public Long getAppointmentFoundId() {
        return AppointmentFoundId;
    }

    public void setAppointmentFoundId(Long appointmentFoundId) {
        AppointmentFoundId = appointmentFoundId;
    }

    public TaskProgram getTaskProgram() {
        return taskProgram;
    }

    public void setTaskProgram(TaskProgram taskProgram) {
        this.taskProgram = taskProgram;
    }    
}
