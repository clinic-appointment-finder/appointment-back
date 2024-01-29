package cl.hcs.finder.appointmentback.model;

import java.util.List;
import java.util.Optional;

public record MedicalAgreementModel(List<DoctorModel> whith, List<DoctorModel> whithout) {
    public record DoctorModel(String code, String name, String urlImage, Optional<Boolean> haveSchedule) {

    }

}
