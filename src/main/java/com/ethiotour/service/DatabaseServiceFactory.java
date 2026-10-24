package com.ethiotour.service;

import com.ethiotour.config.DatabaseConfig;

public class DatabaseServiceFactory {
    private static IDatabaseService instance;

    public static IDatabaseService getDatabaseService() {
        if (instance == null) {
            instance = createDatabaseService();
        }
        return instance;
    }

    private static IDatabaseService createDatabaseService() {
        DatabaseConfig.DatabaseMode mode = DatabaseConfig.getDatabaseMode();
        
        System.out.println("Initializing database service: " + mode.getDisplayName());
        
        switch (mode) {
            case POSTGRESQL:
                try {
                    Class<?> clazz = Class.forName("com.ethiotour.service.PostgreSQLDatabaseService");
                    instance = (IDatabaseService) clazz.getMethod("getInstance").invoke(null);
                    System.out.println("[OK] PostgreSQL database service initialized");
                } catch (Throwable e) {
                    System.err.println("[ERROR] Failed to initialize PostgreSQL, falling back to SQLITE: " + e.getMessage());
                    instance = createSQLiteService();
                }
                break;
            case SQLITE:
                instance = createSQLiteService();
                break;
            case IN_MEMORY:
            default:
                instance = DatabaseService.getInstance();
                System.out.println("[OK] In-memory database service initialized");
                break;
        }
        
        DatabaseConfig.printConfiguration();
        return instance;
    }

    private static IDatabaseService createSQLiteService() {
        try {
            Class<?> clazz = Class.forName("com.ethiotour.service.SQLiteDatabaseService");
            return (IDatabaseService) clazz.getMethod("getInstance").invoke(null);
        } catch (Throwable e) {
            System.err.println("[ERROR] Failed to initialize SQLite, falling back to IN_MEMORY: " + e.getMessage());
            return DatabaseService.getInstance();
        }
    }

    public static void resetInstance() {
        instance = null;
    }
}
