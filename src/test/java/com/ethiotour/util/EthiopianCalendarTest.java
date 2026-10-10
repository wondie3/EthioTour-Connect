package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testEthiopianDateDisplayPagume() {
        // Date in Pagume (13th month of Ethiopian calendar)
        LocalDate pagumeDate = LocalDate.of(2026, 9, 8);
        int[] ethDate = EthiopianCalendar.convertToEthiopian(pagumeDate);
        assertEquals(13, ethDate[1]);

        // Should not throw ArrayIndexOutOfBoundsException and should return "Pagume"
        String display = EthiopianCalendar.getEthiopianDateDisplay(pagumeDate);
        assertNotNull(display);
        assertTrue(display.contains("Pagume"), "Display should contain 'Pagume', but got: " + display);
    }

    @Test
    public void testConvertToGregorianAndBack() {
        LocalDate original = LocalDate.of(2026, 1, 19);
        int[] eth = EthiopianCalendar.convertToEthiopian(original);
        LocalDate convertedBack = EthiopianCalendar.convertToGregorian(eth[0], eth[1], eth[2]);
        assertEquals(original, convertedBack);
    }

    @Test
    public void testIsPeakSeason() {
        assertTrue(EthiopianCalendar.isPeakSeason(LocalDate.of(2026, 10, 15)));
        assertFalse(EthiopianCalendar.isPeakSeason(LocalDate.of(2026, 6, 15)));
    }
}
