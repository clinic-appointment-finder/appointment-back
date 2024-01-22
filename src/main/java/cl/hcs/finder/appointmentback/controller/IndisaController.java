package cl.hcs.finder.appointmentback.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.hcs.finder.appointmentback.model.IndisaAppointmentInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.model.TaskProgram;
import cl.hcs.finder.appointmentback.service.AppointmentDoctorService;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import cl.hcs.finder.appointmentback.service.TaskProgramService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clinic/indisa")
public class IndisaController {

    @Autowired
    private final IndisaServiceInvoker externalServiceInvoker;

    @Autowired
    private final TaskProgramService taskProgramService;

    @Autowired
    private final AppointmentDoctorService appointmentDoctorService;

    public IndisaController(IndisaServiceInvoker externalServiceInvoker, TaskProgramService taskProgramService,
            AppointmentDoctorService appointmentDoctorService) {
        this.externalServiceInvoker = externalServiceInvoker;
        this.taskProgramService = taskProgramService;
        this.appointmentDoctorService = appointmentDoctorService;
    }

    @GetMapping("/external/appointments")
    public Mono<IndisaCalendarOutputModel> invokeExternalService(@RequestParam String agendaID,
            @RequestParam String specialityID,
            @RequestParam String doctorID,
            @RequestParam String office) {
        return externalServiceInvoker
                .invokeExternalIndisaCalendarEndpoint(
                        new IndisaCalendarInputModel(agendaID, specialityID, doctorID, office));
    }

    @PostMapping("/appointments")
    public ResponseEntity<TaskProgram> createTaskProgram(@RequestBody IndisaAppointmentInputModel inputModel) {
        TaskProgram createdTaskProgram = taskProgramService.createTaskProgram(inputModel);
        return new ResponseEntity<>(createdTaskProgram, HttpStatus.CREATED);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<TaskProgram>> findAllTaskProgram() {
        List<TaskProgram> list = taskProgramService.FindAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<TaskProgram> getTaskProgramById(@PathVariable Long id) {
        if (id == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Optional<TaskProgram> taskProgram = taskProgramService.FindByID(id);

        if (taskProgram.isPresent()) {
            return new ResponseEntity<>(taskProgram.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    @PatchMapping("/appointments/{id}")
    public ResponseEntity<TaskProgram> updateTaskProgramActive(@PathVariable Long id,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean isActive = requestBody.get("isActive");
        int cantUpdate = taskProgramService.updateTaskProgramActive(id, isActive);

        if (cantUpdate == 1) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/appointments/{id}/doctors/{doctorId}")
    public ResponseEntity<TaskProgram> updateTaskProgramActive(@PathVariable Long id, @PathVariable Integer doctorId,
            @RequestBody Map<String, Boolean> requestBody) {
        boolean isNotify = requestBody.get("isNotify");
        int cantUpdate = appointmentDoctorService.updateAppointmentDoctorNotify(id, doctorId, isNotify);
        if (cantUpdate == 1) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
