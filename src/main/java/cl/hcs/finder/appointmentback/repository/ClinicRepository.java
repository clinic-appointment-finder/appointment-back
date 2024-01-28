package cl.hcs.finder.appointmentback.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.hcs.finder.appointmentback.entity.Clinic;

public interface ClinicRepository extends JpaRepository<Clinic, Integer> {
    
}
