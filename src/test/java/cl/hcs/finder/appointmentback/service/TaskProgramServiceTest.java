package cl.hcs.finder.appointmentback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import cl.hcs.finder.appointmentback.entity.TaskProgram;
import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.repository.TaskProgramRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class TaskProgramServiceTest {

    @Mock
    private TaskProgramRepository taskProgramRepository;

    private TaskProgramService taskProgramService;

    @BeforeEach
    public void setup() {
        taskProgramService = new TaskProgramService(taskProgramRepository);
    }

    @Test
    public void testCreateTaskProgram() {
        IndisaAppointmentInputModel inputModel = new IndisaAppointmentInputModel(67, "2024-01-15", "2024-01-20", 226,
                new Integer[] { 14655 }, "PROVIDENCIA", new String[] { "test@example.com" });
        TaskProgram savedTask = new TaskProgram();
        savedTask.setTaskProgramId(1L);

        when(taskProgramRepository.save(any(TaskProgram.class))).thenReturn(savedTask);

        Mono<TaskProgram> result = taskProgramService.createTaskProgram(inputModel);

        StepVerifier.create(result)
                .expectNextMatches(task -> task.getTaskProgramId() == 1L)
                .verifyComplete();
    }

    @Test
    public void testFindAll() {
        TaskProgram taskProgram = new TaskProgram();
        taskProgram.setEmails(Collections.singletonList("test@example.com"));
        Page<TaskProgram> page = new PageImpl<>(Collections.singletonList(taskProgram));

        when(taskProgramRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Mono<Page<TaskProgram>> result = taskProgramService.FindAll(0, 5, false, true, "PROVIDENCIA", true);

        StepVerifier.create(result)
                .assertNext(p -> {
                    assertNotNull(p);
                    assertEquals(1, p.getContent().size());
                    assertEquals("txxxt@example.com", p.getContent().get(0).getEmails().get(0));
                })
                .verifyComplete();
    }

    @Test
    public void testFindByID() {
        TaskProgram taskProgram = new TaskProgram();
        taskProgram.setTaskProgramId(1L);

        when(taskProgramRepository.findById(anyLong())).thenReturn(Optional.of(taskProgram));

        Mono<TaskProgram> result = taskProgramService.FindByID(1L);

        StepVerifier.create(result)
                .expectNext(taskProgram)
                .verifyComplete();
    }

    @Test
    public void testUpdateTaskProgramActive() {
        when(taskProgramRepository.updateTaskProgramActive(anyLong(), anyBoolean())).thenReturn(1);

        Mono<Integer> result = taskProgramService.updateTaskProgramActive(1L, true);

        StepVerifier.create(result)
                .expectNext(1)
                .verifyComplete();
    }
}
