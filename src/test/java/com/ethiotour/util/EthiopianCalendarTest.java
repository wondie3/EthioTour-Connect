package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testEthiopianDateDisplayThirteenthMonth() {
        // September 6, 2026 falls into Pagume (the 13th month)
        LocalDate pagumeDate = LocalDate.of(2026, 9, 6);
        assertDoesNotThrow(() -> {
            String display = EthiopianCalendar.getEthiopianDateDisplay(pagumeDate);
            assertNotNull(display);
            assertTrue(display.contains("Pagume"), "Display should contain 'Pagume' for 13th month, but got: " + display);
        });
    }

    @Test
    public void testEthiopianDateConversionRoundTrip() {
        LocalDate original = LocalDate.of(2026, 5, 15);
        int[] eth = EthiopianCalendar.convertToEthiopian(original);
        LocalDate convertedBack = EthiopianCalendar.convertToGregorian(eth[0], eth[1], eth[2]);
        assertEquals(original, convertedBack);
    }

    @Test
    public void testIsEthiopianHoliday() {
        LocalDate meskel = LocalDate.of(2026, 9, 27);
        assertTrue(EthiopianCalendar.isEthiopianHoliday(meskel));
        assertEquals("Meskel", EthiopianCalendar.getHolidayName(meskel));
    }
}
