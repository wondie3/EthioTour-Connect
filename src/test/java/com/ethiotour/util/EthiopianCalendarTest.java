package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testPagumeDateConversionAndDisplay() {
        // In Ethiopian year 2018 (Anchor 2025-09-11 is 2018-01-01)
        // 2026-09-06 is Pagume 1, 2018
        LocalDate pagume1 = LocalDate.of(2026, 9, 6);
        int[] ethDate = EthiopianCalendar.convertToEthiopian(pagume1);
        assertEquals(2018, ethDate[0]);
        assertEquals(13, ethDate[1]);
        assertEquals(1, ethDate[2]);

        String display1 = EthiopianCalendar.getEthiopianDateDisplay(pagume1);
        assertEquals("1 Pagume 2018", display1);

        // 2026-09-10 is Pagume 5, 2018 (last day of 2018)
        LocalDate pagume5 = LocalDate.of(2026, 9, 10);
        int[] ethDate5 = EthiopianCalendar.convertToEthiopian(pagume5);
        assertEquals(2018, ethDate5[0]);
        assertEquals(13, ethDate5[1]);
        assertEquals(5, ethDate5[2]);

        String display5 = EthiopianCalendar.getEthiopianDateDisplay(pagume5);
        assertEquals("5 Pagume 2018", display5);

        // Convert back to Gregorian
        LocalDate greg1 = EthiopianCalendar.convertToGregorian(2018, 13, 1);
        assertEquals(pagume1, greg1);

        LocalDate greg5 = EthiopianCalendar.convertToGregorian(2018, 13, 5);
        assertEquals(pagume5, greg5);
    }

    @Test
    public void testRegularDateDisplay() {
        // Enkutatash 2018 (2025-09-11)
        LocalDate enkutatash = LocalDate.of(2025, 9, 11);
        assertEquals("1 Meskerem 2018", EthiopianCalendar.getEthiopianDateDisplay(enkutatash));
    }

    @Test
    public void testLeapYearRules() {
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2015));
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2019));
        assertFalse(EthiopianCalendar.isEthiopianLeapYear(2018));
    }
}
