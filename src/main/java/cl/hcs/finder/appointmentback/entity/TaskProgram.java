package cl.hcs.finder.appointmentback.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class TaskProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskProgramId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @OneToMany(mappedBy = "taskProgram", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<AppointmentFound> doctors;

    private Integer previsionId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String officeName;

    private Integer specialityId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_program_emails", joinColumns = @JoinColumn(name = "task_program_id"))
    @Column(name = "email_id")
    private List<String> emails;

    private LocalDateTime creationDate;

    private boolean active;

    // Constructor
    public TaskProgram() {
        this.doctors = new ArrayList<>();
    }

    public void addDoctor(AppointmentFound doctor) {
        doctors.add(doctor);
        doctor.setTaskProgram(this);
    }    

    public Long getTaskProgramId() {
        return taskProgramId;
    }

    public void setTaskProgramId(Long taskProgramId) {
        this.taskProgramId = taskProgramId;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Integer getPrevisionId() {
        return previsionId;
    }

    public void setPrevisionId(Integer previsionId) {
        this.previsionId = previsionId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public Integer getSpecialityId() {
        return specialityId;
    }

    public void setSpecialityId(Integer specialityId) {
        this.specialityId = specialityId;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<AppointmentFound> getDoctors() {
        return doctors;
    }

    public void setDoctors(List<AppointmentFound> doctors) {
        this.doctors = doctors;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

}
