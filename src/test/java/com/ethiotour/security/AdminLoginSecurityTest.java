package com.ethiotour.security;

import com.ethiotour.view.AdminLoginView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminLoginSecurityTest {

    @BeforeEach
    @AfterEach
    public void cleanupProperties() {
        System.clearProperty("admin.username");
        System.clearProperty("admin.password");
        System.clearProperty("admin.password.hash");
    }

    @Test
    public void testDefaultAdminCredentials() {
        assertEquals("admin", AdminLoginView.getExpectedAdminUsername());
        String defaultHash = AdminLoginView.getExpectedAdminPasswordHash();
        assertNotNull(defaultHash);
        assertTrue(PasswordHasher.verifyPassword("admin123", defaultHash));
        assertFalse(PasswordHasher.verifyPassword("wrongpass", defaultHash));
    }

    @Test
    public void testConfigurableAdminUsernameAndPassword() {
        System.setProperty("admin.username", "superadmin");
        System.setProperty("admin.password", "SuperSecret999!");

        assertEquals("superadmin", AdminLoginView.getExpectedAdminUsername());
        String configuredHash = AdminLoginView.getExpectedAdminPasswordHash();
        assertNotNull(configuredHash);
        assertTrue(PasswordHasher.verifyPassword("SuperSecret999!", configuredHash));
        assertFalse(PasswordHasher.verifyPassword("admin123", configuredHash));
    }

    @Test
    public void testConfigurableAdminPasswordHashDirectly() {
        String customHash = PasswordHasher.hashPassword("CustomSaltedHash!23");
        System.setProperty("admin.password.hash", customHash);

        assertEquals(customHash, AdminLoginView.getExpectedAdminPasswordHash());
        assertTrue(PasswordHasher.verifyPassword("CustomSaltedHash!23", AdminLoginView.getExpectedAdminPasswordHash()));
    }
}
