package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testPagumeDisplayDoesNotThrowOutOfBounds() {
        // 2018 Pagume 1 is Sept 6, 2026
        LocalDate pagume1 = LocalDate.of(2026, 9, 6);
        String display = EthiopianCalendar.getEthiopianDateDisplay(pagume1);
        assertNotNull(display);
        assertTrue(display.contains("Pagume"), "Display should contain Pagume, got: " + display);
        assertEquals("1 Pagume 2018", display);

        // Pagume 5 is Sept 10, 2026
        LocalDate pagume5 = LocalDate.of(2026, 9, 10);
        String display5 = EthiopianCalendar.getEthiopianDateDisplay(pagume5);
        assertEquals("5 Pagume 2018", display5);
    }

    @Test
    public void testMeskeremNewYearDisplay() {
        // Ethiopian New Year (1 Meskerem 2018) is Sept 11, 2025
        LocalDate enkutatash = LocalDate.of(2025, 9, 11);
        String display = EthiopianCalendar.getEthiopianDateDisplay(enkutatash);
        assertEquals("1 Meskerem 2018", display);

        // 1 Meskerem 2019 is Sept 11, 2026
        LocalDate enkutatash2019 = LocalDate.of(2026, 9, 11);
        String display2019 = EthiopianCalendar.getEthiopianDateDisplay(enkutatash2019);
        assertEquals("1 Meskerem 2019", display2019);
    }

    @Test
    public void testConvertToGregorianAndBack() {
        // 2018 Meskerem 1 -> 2025-09-11
        LocalDate greg = EthiopianCalendar.convertToGregorian(2018, 1, 1);
        assertEquals(LocalDate.of(2025, 9, 11), greg);

        int[] eth = EthiopianCalendar.convertToEthiopian(greg);
        assertEquals(2018, eth[0]);
        assertEquals(1, eth[1]);
        assertEquals(1, eth[2]);

        // 2018 Pagume 1 (Month 13, Day 1) -> 2026-09-06
        LocalDate gregPagume = EthiopianCalendar.convertToGregorian(2018, 13, 1);
        assertEquals(LocalDate.of(2026, 9, 6), gregPagume);

        int[] ethPagume = EthiopianCalendar.convertToEthiopian(gregPagume);
        assertEquals(2018, ethPagume[0]);
        assertEquals(13, ethPagume[1]);
        assertEquals(1, ethPagume[2]);
    }

    @Test
    public void testHolidayAndPeakSeason() {
        LocalDate timkat = LocalDate.of(2026, 1, 19);
        assertTrue(EthiopianCalendar.isEthiopianHoliday(timkat));
        assertEquals("Timkat", EthiopianCalendar.getHolidayName(timkat));
        assertTrue(EthiopianCalendar.isPeakSeason(timkat));

        LocalDate regularDate = LocalDate.of(2026, 7, 15);
        assertFalse(EthiopianCalendar.isEthiopianHoliday(regularDate));
        assertNull(EthiopianCalendar.getHolidayName(regularDate));
        assertFalse(EthiopianCalendar.isPeakSeason(regularDate));
    }

    @Test
    public void testInvalidEthiopianDateValidation() {
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(2018, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(2018, 14, 1));
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(2018, 1, 31));
        // Pagume non-leap year (2018 % 4 == 2) max day is 5
        assertThrows(IllegalArgumentException.class, () -> EthiopianCalendar.convertToGregorian(2018, 13, 6));
    }
}
