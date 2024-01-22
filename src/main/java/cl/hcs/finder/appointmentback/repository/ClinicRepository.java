package cl.hcs.finder.appointmentback.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.hcs.finder.appointmentback.model.Clinic;

public interface ClinicRepository extends JpaRepository<Clinic, Integer> {
    
}
