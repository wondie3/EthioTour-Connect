package com.ethiotour.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility for secure password hashing and verification using PBKDF2 with SHA-256.
 * Implements salted password hashing and constant-time comparison to prevent timing attacks.
 */
public class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final int ITERATIONS = 65536;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a cryptographically secure random salt.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password with a given salt using PBKDF2WithHmacSHA256.
     */
    public static String hashPassword(char[] password, byte[] salt) {
        PBEKeySpec spec = null;
        try {
            spec = new PBEKeySpec(password, salt, ITERATIONS, HASH_BYTES * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }

    /**
     * Hashes a password string with a new random salt and returns salt:hash.
     */
    public static String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return hashPassword(password.toCharArray(), generateSalt());
    }

    /**
     * Verifies a password against a stored formatted hash (salt:hash).
     * Uses constant-time equality check to prevent timing attacks.
     */
    public static boolean verifyPassword(char[] password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }

        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, expectedHash.length * 8);
            byte[] computedHash;
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
                computedHash = factory.generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }

            return constantTimeEquals(expectedHash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifies a string password against a stored formatted hash (salt:hash).
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null) {
            return false;
        }
        return verifyPassword(password.toCharArray(), storedHash);
    }

    /**
     * Constant-time array comparison to mitigate timing attacks.
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
