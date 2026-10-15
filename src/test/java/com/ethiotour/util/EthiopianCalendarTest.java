package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testPagumeDateDisplay() {
        // Ethiopian 13th month Pagume falls around Sept 6-10 in Gregorian calendar
        LocalDate pagumeDate = LocalDate.of(2025, 9, 6);
        String display = EthiopianCalendar.getEthiopianDateDisplay(pagumeDate);

        assertNotNull(display);
        assertTrue(display.contains("Pagume"), "Display should contain month name 'Pagume': " + display);
    }

    @Test
    public void testGregorianToEthiopianConversion() {
        LocalDate date = LocalDate.of(2025, 9, 11); // Enkutatash (Ethiopian New Year 2018)
        int[] ethiopianDate = EthiopianCalendar.convertToEthiopian(date);

        assertEquals(2018, ethiopianDate[0]);
        assertEquals(1, ethiopianDate[1]);
        assertEquals(1, ethiopianDate[2]);
    }

    @Test
    public void testEthiopianHolidayCheck() {
        LocalDate meskel = LocalDate.of(2026, 9, 27);
        assertTrue(EthiopianCalendar.isEthiopianHoliday(meskel));
        assertEquals("Meskel", EthiopianCalendar.getHolidayName(meskel));
    }
}
