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
        assertEquals(13, ethDate[1], "Month should be 13 (Pagume)");

        String display = EthiopianCalendar.getEthiopianDateDisplay(dateInPagumen);
        assertNotNull(display);
        assertTrue(display.contains("Pagume"), "Display string should contain Pagume: " + display);
    }

    @Test
    public void testGetEthiopianDateDisplayMeskerem() {
        LocalDate dateInMeskerem = LocalDate.of(2026, 9, 11);
        String display = EthiopianCalendar.getEthiopianDateDisplay(dateInMeskerem);
        assertNotNull(display);
        assertTrue(display.contains("Meskerem"), "Display string should contain Meskerem: " + display);
    }

    @Test
    public void testConvertToGregorianAndBack() {
        LocalDate original = LocalDate.of(2026, 1, 19);
        int[] eth = EthiopianCalendar.convertToEthiopian(original);
        LocalDate convertedBack = EthiopianCalendar.convertToGregorian(eth[0], eth[1], eth[2]);
        assertEquals(original, convertedBack);
    }

    @Test
    public void testHolidayAndPeakSeason() {
        LocalDate timkat = LocalDate.of(2026, 1, 19);
        assertTrue(EthiopianCalendar.isEthiopianHoliday(timkat));
        assertEquals("Timkat", EthiopianCalendar.getHolidayName(timkat));
        assertTrue(EthiopianCalendar.isPeakSeason(timkat));

        LocalDate regularDate = LocalDate.of(2026, 5, 10);
        assertFalse(EthiopianCalendar.isEthiopianHoliday(regularDate));
        assertNull(EthiopianCalendar.getHolidayName(regularDate));
        assertFalse(EthiopianCalendar.isPeakSeason(regularDate));
    }
}
