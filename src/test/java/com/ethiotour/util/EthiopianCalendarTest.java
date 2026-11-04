package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testGetEthiopianDateDisplayPagumen() {
        // September 6 in Gregorian is Pagumen 1 in Ethiopian calendar (month 13)
        LocalDate dateInPagumen = LocalDate.of(2026, 9, 6);
        int[] ethDate = EthiopianCalendar.convertToEthiopian(dateInPagumen);
        assertEquals(13, ethDate[1], "Month should be 13 (Pagumen)");

        String display = EthiopianCalendar.getEthiopianDateDisplay(dateInPagumen);
        assertNotNull(display);
        assertTrue(display.contains("Pagumen"), "Display string should contain Pagumen: " + display);
    }

    @Test
    public void testGetEthiopianDateDisplayMeskerem() {
        LocalDate dateInMeskerem = LocalDate.of(2026, 9, 11);
        String display = EthiopianCalendar.getEthiopianDateDisplay(dateInMeskerem);
        assertNotNull(display);
        assertTrue(display.contains("Meskerem"), "Display string should contain Meskerem: " + display);
    }
}
