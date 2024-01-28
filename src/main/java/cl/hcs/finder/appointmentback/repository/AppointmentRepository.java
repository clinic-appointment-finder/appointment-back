package cl.hcs.finder.appointmentback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.hcs.finder.appointmentback.entity.AppointmentFound;

public interface AppointmentRepository extends JpaRepository<AppointmentFound, Long> {

    @Modifying
    @Query("UPDATE AppointmentFound t SET t.isNotify = :is_notify WHERE t.taskProgram.taskProgramId = :task_program_id and t.doctorId = :doctor_id")
    int updateAppointmentDoctorNotify(@Param("task_program_id") Long taskProgramId,
            @Param("doctor_id") Integer doctorId,
            @Param("is_notify") boolean is_notify);
}
