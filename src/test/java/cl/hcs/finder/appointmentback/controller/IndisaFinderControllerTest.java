package cl.hcs.finder.appointmentback.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.hcs.finder.appointmentback.model.IndisaCalendarOutputModel;

class IndisaFinderControllerTest {

    @Test
    void returnsDatesNotPresentInUnavailableDatesInAscendingOrder() {
        IndisaCalendarOutputModel availableResponse = new IndisaCalendarOutputModel(
                new String[] { "2026-08-09" }, 90, "08/05/2026", false, "ok", "20260804",
                new String[] { "2026-08-10" });

        assertEquals(List.of("2026-08-05", "2026-08-06", "2026-08-07", "2026-08-08"),
                IndisaFinderController.extractAvailableDates(
                        availableResponse, LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 9)));
    }

    @Test
    void acceptsAllHoursWhenRangeIsNotConfigured() {
        assertTrue(IndisaFinderController.isWithinConfiguredRange(
                "08:00", null, null));
    }

    @Test
    void acceptsHoursInsideInclusiveRange() {
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(18, 0);

        assertTrue(IndisaFinderController.isWithinConfiguredRange("09:00", start, end));
        assertTrue(IndisaFinderController.isWithinConfiguredRange("18:00", start, end));
        assertFalse(IndisaFinderController.isWithinConfiguredRange("08:59", start, end));
        assertFalse(IndisaFinderController.isWithinConfiguredRange("18:01", start, end));
    }

    @Test
    void supportsOnlyStartOrOnlyEnd() {
        assertTrue(IndisaFinderController.isWithinConfiguredRange(
                "10:00", LocalTime.of(9, 0), null));
        assertFalse(IndisaFinderController.isWithinConfiguredRange(
                "08:59", LocalTime.of(9, 0), null));
        assertTrue(IndisaFinderController.isWithinConfiguredRange(
                "18:00", null, LocalTime.of(18, 0)));
        assertFalse(IndisaFinderController.isWithinConfiguredRange(
                "18:01", null, LocalTime.of(18, 0)));
    }
}
