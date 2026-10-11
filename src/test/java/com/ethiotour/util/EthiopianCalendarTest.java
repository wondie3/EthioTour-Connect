package com.ethiotour.util;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testGetEthiopianDateDisplayNormalMonth() {
        LocalDate date = LocalDate.of(2025, 9, 11);
        String display = EthiopianCalendar.getEthiopianDateDisplay(date);
        assertNotNull(display);
        assertTrue(display.contains("Meskerem"));
        assertEquals("1 Meskerem 2018", display);
    }

    @Test
    public void testGetEthiopianDateDisplayThirteenthMonthPagume() {
        // Date falling in month 13 (Pagume 1, 2017 = Sept 6, 2025)
        LocalDate date = LocalDate.of(2025, 9, 6);
        assertDoesNotThrow(() -> {
            String display = EthiopianCalendar.getEthiopianDateDisplay(date);
            assertNotNull(display);
            assertTrue(display.contains("Pagume"));
            assertEquals("1 Pagume 2017", display);
        });
    }

    @Test
    public void testConvertToGregorianAndBack() {
        // 2018 Meskerem 1 => 2025-09-11
        LocalDate greg = EthiopianCalendar.convertToGregorian(2018, 1, 1);
        assertEquals(LocalDate.of(2025, 9, 11), greg);

        int[] eth = EthiopianCalendar.convertToEthiopian(greg);
        assertEquals(2018, eth[0]);
        assertEquals(1, eth[1]);
        assertEquals(1, eth[2]);
    }

    @Test
    public void testConvertToGregorianPagume() {
        // 2017 Pagume 5
        LocalDate greg = EthiopianCalendar.convertToGregorian(2017, 13, 5);
        assertEquals(LocalDate.of(2025, 9, 10), greg);

        int[] eth = EthiopianCalendar.convertToEthiopian(greg);
        assertEquals(2017, eth[0]);
        assertEquals(13, eth[1]);
        assertEquals(5, eth[2]);
    }

    @Test
    public void testIsEthiopianLeapYear() {
        // Year % 4 == 3 is leap year in Ethiopian calendar
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2015));
        assertFalse(EthiopianCalendar.isEthiopianLeapYear(2018));
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

    @Test
    public void testInvalidEthiopianDate() {
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(2018, 14, 1));
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(2018, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(2018, 1, 32));
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(-1, 1, 1));
    }
}
