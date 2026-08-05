package cl.hcs.finder.appointmentback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class IndisaServiceInvokerTest {

    @Test
    void extractsAvailableHoursFromIndisaHtml() {
        String html = "<li onclick=\"get_date_selected('2026-08-05 08:00')\"><span class=\"hour\">08:00 Hrs.</span></li>"
                + "<li onclick=\"get_date_selected('2026-08-05 10:20')\"><span class=\"hour\">10:20 Hrs.</span></li>"
                + "<li onclick=\"get_date_selected('2026-08-05 10:20')\"><span class=\"hour\">10:20 Hrs.</span></li>";

        assertEquals(List.of("08:00", "10:20"), IndisaServiceInvoker.extractAvailableHours(html));
    }

    @Test
    void acceptsStructuredEmptyDataFromIndisa() throws Exception {
        assertEquals(List.of(), IndisaServiceInvoker.extractAvailableHours(
                new ObjectMapper().readTree("{}")));
    }
}
