package com.ethiotour.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class EthiopianCalendar {
    private static final LocalDate ETHIOPIAN_ANCHOR_GREGORIAN = LocalDate.of(2025, 9, 11);
    private static final int ETHIOPIAN_ANCHOR_YEAR = 2018;
    private static final Map<String, LocalDate> ethiopianHolidays = new HashMap<>();
    
    static {
        // Initialize major Ethiopian holidays (these would be calculated properly in production)
        ethiopianHolidays.put("Meskel", LocalDate.of(2026, 9, 27));
        ethiopianHolidays.put("Timkat", LocalDate.of(2026, 1, 19));
        ethiopianHolidays.put("Enkutatash", LocalDate.of(2026, 9, 11));
        ethiopianHolidays.put("Mawlid", LocalDate.of(2026, 9, 4));
    }
    
    public static LocalDate convertToGregorian(int ethiopianYear, int ethiopianMonth, int ethiopianDay) {
        validateEthiopianDate(ethiopianYear, ethiopianMonth, ethiopianDay);

        long days = daysBeforeEthiopianYear(ethiopianYear) - daysBeforeEthiopianYear(ETHIOPIAN_ANCHOR_YEAR);
        days += (long) (ethiopianMonth - 1) * 30;
        days += ethiopianDay - 1L;

        return ETHIOPIAN_ANCHOR_GREGORIAN.plusDays(days);
    }
    
    public static int[] convertToEthiopian(LocalDate gregorianDate) {
        long daysFromAnchor = ChronoUnit.DAYS.between(ETHIOPIAN_ANCHOR_GREGORIAN, gregorianDate);
        int ethiopianYear = ETHIOPIAN_ANCHOR_YEAR;

        if (daysFromAnchor >= 0) {
            while (daysFromAnchor >= lengthOfEthiopianYear(ethiopianYear)) {
                daysFromAnchor -= lengthOfEthiopianYear(ethiopianYear);
                ethiopianYear++;
            }
        } else {
            while (daysFromAnchor < 0) {
                ethiopianYear--;
                daysFromAnchor += lengthOfEthiopianYear(ethiopianYear);
            }
        }

        int ethiopianMonth = (int) (daysFromAnchor / 30) + 1;
        int ethiopianDay = (int) (daysFromAnchor % 30) + 1;

        return new int[]{ethiopianYear, ethiopianMonth, ethiopianDay};
    }
    
    public static String getEthiopianDateDisplay(LocalDate gregorianDate) {
        int[] ethiopianDate = convertToEthiopian(gregorianDate);
        String[] ethiopianMonths = {
            "Meskerem", "Tekemt", "Hidar", "Tahsas", 
            "Tir", "Yekatit", "Megabit", "Miyazya", 
            "Ginbot", "Sene", "Hamle", "Nehase"
        };
        
        return ethiopianDate[2] + " " + ethiopianMonths[ethiopianDate[1] - 1] + " " + ethiopianDate[0];
    }
    
    public static boolean isEthiopianHoliday(LocalDate date) {
        return ethiopianHolidays.containsValue(date);
    }
    
    public static String getHolidayName(LocalDate date) {
        for (Map.Entry<String, LocalDate> entry : ethiopianHolidays.entrySet()) {
            if (entry.getValue().equals(date)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public static boolean isPeakSeason(LocalDate date) {
        // Peak seasons around major holidays and festivals
        int month = date.getMonthValue();
        return (month >= 9 && month <= 11) || (month >= 1 && month <= 3);
    }
    
    public static long getDaysUntilHoliday(LocalDate date, String holidayName) {
        LocalDate holiday = ethiopianHolidays.get(holidayName);
        if (holiday != null) {
            return ChronoUnit.DAYS.between(date, holiday);
        }
        return -1;
    }

    public static boolean isEthiopianLeapYear(int ethiopianYear) {
        return ethiopianYear % 4 == 3;
    }

    private static int lengthOfEthiopianYear(int ethiopianYear) {
        return isEthiopianLeapYear(ethiopianYear) ? 366 : 365;
    }

    private static long daysBeforeEthiopianYear(int ethiopianYear) {
        long previousYears = ethiopianYear - 1L;
        return previousYears * 365 + previousYears / 4;
    }

    private static void validateEthiopianDate(int year, int month, int day) {
        if (year < 1) {
            throw new IllegalArgumentException("Ethiopian year must be positive");
        }
        if (month < 1 || month > 13) {
            throw new IllegalArgumentException("Ethiopian month must be between 1 and 13");
        }

        int maxDay = month == 13 ? (isEthiopianLeapYear(year) ? 6 : 5) : 30;
        if (day < 1 || day > maxDay) {
            throw new IllegalArgumentException("Ethiopian day must be between 1 and " + maxDay);
        }
    }
}
