package cl.hcs.finder.appointmentback.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.hcs.finder.appointmentback.model.IndisaCalendarInputModel;
import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;
import cl.hcs.finder.appointmentback.service.IndisaServiceInvoker;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/clinic/indisa")
public class IndisaController {

    @Autowired
    private final IndisaServiceInvoker externalServiceInvoker;

    public IndisaController(IndisaServiceInvoker externalServiceInvoker) {
        this.externalServiceInvoker = externalServiceInvoker;
    }

    @GetMapping("/appointments")
    public Mono<IndisaCalendarOutputModel> invokeExternalService(@RequestParam String agendaID,
            @RequestParam String specialityID,
            @RequestParam String doctorID,
            @RequestParam String office) {
        return externalServiceInvoker
                .invokeExternalIndisaCalendarEndpoint(new IndisaCalendarInputModel(agendaID, specialityID, doctorID, office));
    }

}
