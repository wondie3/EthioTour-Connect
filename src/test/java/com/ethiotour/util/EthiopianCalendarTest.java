package com.ethiotour.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class EthiopianCalendarTest {

    @Test
    public void testPagumeDisplayDoesNotThrow() {
        // Pagume 1, 2018 is 360 days after Meskerem 1, 2018 (2025-09-11), which is 2026-09-06
        LocalDate pagume1 = LocalDate.of(2026, 9, 6);
        int[] ethiopianDate = EthiopianCalendar.convertToEthiopian(pagume1);
        assertEquals(2018, ethiopianDate[0]);
        assertEquals(13, ethiopianDate[1]);
        assertEquals(1, ethiopianDate[2]);

        String display = EthiopianCalendar.getEthiopianDateDisplay(pagume1);
        assertEquals("1 Pagume 2018", display);
    }

    @Test
    public void testGregorianToEthiopianRoundTrip() {
        LocalDate orig = LocalDate.of(2026, 1, 19);
        int[] eth = EthiopianCalendar.convertToEthiopian(orig);
        LocalDate convertedBack = EthiopianCalendar.convertToGregorian(eth[0], eth[1], eth[2]);
        assertEquals(orig, convertedBack);
    }
}
