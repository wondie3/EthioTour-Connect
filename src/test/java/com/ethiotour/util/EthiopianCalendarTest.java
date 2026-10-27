package com.ethiotour.util;

import org.junit.Test;
import java.time.LocalDate;
import static org.junit.Assert.*;

public class EthiopianCalendarTest {

    @Test
    public void testConvertEthiopianToGregorianAndBack() {
        // Ethiopian 2018-01-01 -> Gregorian 2025-09-11
        LocalDate greg = EthiopianCalendar.convertToGregorian(2018, 1, 1);
        assertEquals(LocalDate.of(2025, 9, 11), greg);

        int[] ethiopianDate = EthiopianCalendar.convertToEthiopian(greg);
        assertEquals(2018, ethiopianDate[0]);
        assertEquals(1, ethiopianDate[1]);
        assertEquals(1, ethiopianDate[2]);
    }

    @Test
    public void testEthiopianLeapYearConversion() {
        // Ethiopian 2019 is a leap year (2019 % 4 == 3). Month 13 has 6 days.
        assertTrue(EthiopianCalendar.isEthiopianLeapYear(2019));

        LocalDate gregPagume6 = EthiopianCalendar.convertToGregorian(2019, 13, 6);
        int[] ethDate = EthiopianCalendar.convertToEthiopian(gregPagume6);

        assertEquals(2019, ethDate[0]);
        assertEquals(13, ethDate[1]);
        assertEquals(6, ethDate[2]);

        // Next day in Gregorian must be Ethiopian 2020-01-01
        LocalDate gregNewYear = gregPagume6.plusDays(1);
        int[] ethNewYear = EthiopianCalendar.convertToEthiopian(gregNewYear);

        assertEquals(2020, ethNewYear[0]);
        assertEquals(1, ethNewYear[1]);
        assertEquals(1, ethNewYear[2]);
    }
}
