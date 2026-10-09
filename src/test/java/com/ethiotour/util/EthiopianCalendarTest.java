package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testGetEthiopianDateDisplayPagume() {
        // Sep 6, 2026 corresponds to Pagume 1, 2018 EE (since 2018 EE is non-leap or leap)
        LocalDate pagumeDate = LocalDate.of(2026, 9, 6);
        String display = EthiopianCalendar.getEthiopianDateDisplay(pagumeDate);

        assertNotNull(display);
        assertTrue(display.contains("Pagume"), "Display string should contain Pagume for month 13, got: " + display);
    }

    @Test
    public void testGetEthiopianDateDisplayMeskerem() {
        LocalDate newYearDate = LocalDate.of(2025, 9, 11);
        String display = EthiopianCalendar.getEthiopianDateDisplay(newYearDate);

        assertNotNull(display);
        assertTrue(display.contains("Meskerem"), "Display string should contain Meskerem for month 1, got: " + display);
    }

    @Test
    public void testEthiopianLeapYear() {
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2015));
        assertFalse(EthiopianCalendar.isEthiopianLeapYear(2016));
        assertFalse(EthiopianCalendar.isEthiopianLeapYear(2017));
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2019));
    }

    @Test
    public void testGregorianAndEthiopianConversionRoundTrip() {
        LocalDate original = LocalDate.of(2026, 1, 19);
        int[] eth = EthiopianCalendar.convertToEthiopian(original);
        LocalDate convertedBack = EthiopianCalendar.convertToGregorian(eth[0], eth[1], eth[2]);

        assertEquals(original, convertedBack);
    }

    @Test
    public void testHolidayChecks() {
        LocalDate timkat = LocalDate.of(2026, 1, 19);
        assertTrue(EthiopianCalendar.isEthiopianHoliday(timkat));
        assertEquals("Timkat", EthiopianCalendar.getHolidayName(timkat));
        assertNull(EthiopianCalendar.getHolidayName(LocalDate.of(2026, 5, 5)));
    }
}
