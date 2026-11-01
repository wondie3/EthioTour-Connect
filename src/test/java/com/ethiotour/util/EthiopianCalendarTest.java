package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testGetEthiopianDateDisplayPagumen() {
        // September 6, 2026 is Pagumen 1, 2018 in Ethiopian calendar (month 13)
        LocalDate pagumenDate = LocalDate.of(2026, 9, 6);
        int[] eth = EthiopianCalendar.convertToEthiopian(pagumenDate);
        assertEquals(13, eth[1], "Month should be 13 (Pagumen)");

        // Prior to the fix, this threw ArrayIndexOutOfBoundsException
        String display = EthiopianCalendar.getEthiopianDateDisplay(pagumenDate);
        assertNotNull(display);
        assertTrue(display.contains("Pagumen"), "Display should contain 'Pagumen': " + display);
    }

    @Test
    public void testGetEthiopianDateDisplayMeskerem() {
        LocalDate dateInMeskerem = LocalDate.of(2026, 9, 11);
        String display = EthiopianCalendar.getEthiopianDateDisplay(dateInMeskerem);
        assertNotNull(display);
        assertTrue(display.contains("Meskerem"), "Display should contain 'Meskerem': " + display);
    }
}
