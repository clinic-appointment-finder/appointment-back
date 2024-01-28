package cl.hcs.finder.appointmentback.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Clinic {     
    
    @Id
    private int clinicId;
    
    public Clinic(){}

    public Clinic(int clinicId, String name) {
        this.clinicId = clinicId;
        this.name = name;
    }

    private String name;

    public int getClinicId() {
        return clinicId;
    }

    public void setClinicId(int clinicId) {
        this.clinicId = clinicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }   
    
}
