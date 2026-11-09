package com.nhannkl.db_backup_utility.model;

public record ConnectionConfig(DatabaseType databaseType, String host, int port, String username, String password,
        String database) {
    public ConnectionConfig {
        if (databaseType == null) {
            throw new IllegalArgumentException("Database Type is required.");
        }

        if (database == null || database.isBlank()) {
            throw new IllegalArgumentException("Database Name is required");
        }

        if (host == null || host.isBlank()) {
            host = "localhost";
        }

        if (port <= 0) {
            port = databaseType.getDefaultPort();
        }
    }

    @Override
    public String toString() {
        return "ConnectionConfig[type=%s, host=%s, port=%d, username=%s, password=****, database=%s]"
                .formatted(databaseType, host, port, username, database);
    }
}
