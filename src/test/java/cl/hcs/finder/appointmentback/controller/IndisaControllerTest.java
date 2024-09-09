// package cl.hcs.finder.appointmentback.controller;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.MockitoJUnitRunner;
// import org.mockito.junit.jupiter.MockitoExtension;

// import cl.hcs.finder.appointmentback.entity.TaskProgram;
// import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
// import cl.hcs.finder.appointmentback.repository.TaskProgramRepository;
// import cl.hcs.finder.appointmentback.service.TaskProgramService;
// import reactor.core.publisher.Mono;
// import reactor.test.StepVerifier;

// @ExtendWith(MockitoExtension.class)
// public class IndisaControllerTest {

//     @Mock
//     private TaskProgramRepository taskProgramRepository;

//     @InjectMocks
//     private TaskProgramService taskProgramService;

//     @Test
//     public void testCreateTaskProgram() {
//         // Configuración de datos de prueba
//         IndisaAppointmentInputModel inputModel = new IndisaAppointmentInputModel();
//         TaskProgram expectedTaskProgram = new TaskProgram(/* Datos de prueba */);
//         when(taskProgramRepository.save(any(TaskProgram.class))).thenReturn(expectedTaskProgram);

//         // Llamada al método del controlador
//         Mono<ResponseEntity<TaskProgram>> result = indisaController.createTaskProgram(inputModel);

//         // Verificación de la respuesta
//         StepVerifier.create(result)
//             .expectNext(ResponseEntity.status(HttpStatus.CREATED).body(expectedTaskProgram))
//             .verifyComplete();
//     }
