package cl.hcs.finder.appointmentback.initializers;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cl.hcs.finder.appointmentback.common.Helper;
import cl.hcs.finder.appointmentback.model.Clinic;
import cl.hcs.finder.appointmentback.repository.ClinicRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = Helper.getLogger();

    private final ClinicRepository clinicRepository;

    @Autowired
    public DataInitializer(ClinicRepository clinicRepository) {
        this.clinicRepository = clinicRepository;
    }

    @Override
    public void run(String... args) {
        initializeClinics();
    }

    private void initializeClinics() {        
        // Verificar si ya existen clínicas en la base de datos
        if (clinicRepository.count() == 0) {
            Clinic clinic1 = new Clinic(1, "Clínica Indisa");
            Clinic clinic2 = new Clinic(2, "Clínica Santa María");

            clinicRepository.save(clinic1);
            clinicRepository.save(clinic2);

            log.info("Registros de clínicas creados con éxito.");
        } else {
            log.info("Ya existen registros de clínicas en la base de datos.");
        }
    }
}
