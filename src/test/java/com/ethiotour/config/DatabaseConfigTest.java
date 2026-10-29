package com.ethiotour.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConfigTest {

    private String originalDbMode;

    @BeforeEach
    public void setUp() {
        originalDbMode = System.getProperty("db.mode");
    }

    @AfterEach
    public void tearDown() {
        if (originalDbMode != null) {
            System.setProperty("db.mode", originalDbMode);
        } else {
            System.clearProperty("db.mode");
        }
    }

    @Test
    public void testSystemPropertyPrecedence() {
        System.setProperty("db.mode", "IN_MEMORY");
        assertEquals("IN_MEMORY", DatabaseConfig.getProperty("db.mode"));
        assertEquals(DatabaseConfig.DatabaseMode.IN_MEMORY, DatabaseConfig.getDatabaseMode());

        System.setProperty("db.mode", "SQLITE");
        assertEquals("SQLITE", DatabaseConfig.getProperty("db.mode"));
        assertEquals(DatabaseConfig.DatabaseMode.SQLITE, DatabaseConfig.getDatabaseMode());
    }

    @Test
    public void testMaskSensitiveValue() {
        assertEquals("[NOT SET]", DatabaseConfig.maskSensitiveValue(null));
        assertEquals("[NOT SET]", DatabaseConfig.maskSensitiveValue(""));
        assertEquals("****", DatabaseConfig.maskSensitiveValue("123"));
        assertEquals("pa****rd", DatabaseConfig.maskSensitiveValue("password"));
    }

    @Test
    public void testDefaultGetters() {
        assertNotNull(DatabaseConfig.getAPIBaseUrl());
        assertNotNull(DatabaseConfig.getChapaCurrency());
        assertTrue(DatabaseConfig.getPoolMaxSize() > 0);
    }
}
