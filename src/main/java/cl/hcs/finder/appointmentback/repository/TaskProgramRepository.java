package cl.hcs.finder.appointmentback.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cl.hcs.finder.appointmentback.entity.TaskProgram;

@Repository
@Transactional
public interface TaskProgramRepository extends JpaRepository<TaskProgram, Long>, JpaSpecificationExecutor<TaskProgram> {

    @Modifying
    @Query("UPDATE TaskProgram t SET t.active = :active WHERE t.taskProgramId = :id")
    int updateTaskProgramActive(@Param("id") Long id, @Param("active") boolean active);

}
