package com.ethiotour.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testGetEthiopianDateDisplayPagumeMonth() {
        // September 6, 2026 corresponds to Pagume 1, 2018
        LocalDate pagumeDate = LocalDate.of(2026, 9, 6);
        String display = EthiopianCalendar.getEthiopianDateDisplay(pagumeDate);

        assertNotNull(display);
        assertTrue(display.contains("Pagume"), "Display string should contain 'Pagume', but got: " + display);
        assertEquals("1 Pagume 2018", display);
    }

    @Test
    public void testLeapYearConversionsRoundTrip() {
        // Test round trip conversions across multiple years including leap years (2015, 2019)
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2028, 1, 1);

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            int[] ethDate = EthiopianCalendar.convertToEthiopian(current);
            LocalDate convertedBack = EthiopianCalendar.convertToGregorian(ethDate[0], ethDate[1], ethDate[2]);
            assertEquals(current, convertedBack, "Round trip conversion failed for: " + current);
            current = current.plusDays(1);
        }
    }

    @Test
    public void testIsEthiopianLeapYear() {
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2015));
        assertFalse(EthiopianCalendar.isEthiopianLeapYear(2016));
        assertFalse(EthiopianCalendar.isEthiopianLeapYear(2017));
        assertFalse(EthiopianCalendar.isEthiopianLeapYear(2018));
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2019));
    }
}
