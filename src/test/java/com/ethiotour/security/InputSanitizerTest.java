package com.ethiotour.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InputSanitizerTest {

    @Test
    public void testSanitizeStringWithNestedTags() {
        String nestedTagInput = "<<script>script>alert('xss')</<script>script>";
        String result = InputSanitizer.sanitizeString(nestedTagInput);
        assertEquals("alert('xss')", result);
    }

    @Test
    public void testSanitizeStringNormal() {
        String normalInput = "<b>Hello</b> <i>World</i>";
        String result = InputSanitizer.sanitizeString(normalInput);
        assertEquals("Hello World", result);
    }

    @Test
    public void testSanitizeStringNullAndEmpty() {
        assertEquals("", InputSanitizer.sanitizeString(null));
        assertEquals("", InputSanitizer.sanitizeString("   "));
    }

    @Test
    public void testEmailValidationAndSanitization() {
        assertTrue(InputSanitizer.isValidEmail("test@example.com"));
        assertFalse(InputSanitizer.isValidEmail("invalid-email"));
        assertEquals("test@example.com", InputSanitizer.sanitizeEmail(" TEST@EXAMPLE.COM "));
    }

    @Test
    public void testPhoneValidationAndSanitization() {
        assertTrue(InputSanitizer.isValidPhone("+251911121314"));
        assertFalse(InputSanitizer.isValidPhone("123"));
        assertEquals("+251911121314", InputSanitizer.sanitizePhone("+251 (911) 12-13-14"));
    }
}
